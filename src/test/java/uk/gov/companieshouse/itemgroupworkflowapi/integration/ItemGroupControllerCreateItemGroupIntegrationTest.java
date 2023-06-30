package uk.gov.companieshouse.itemgroupworkflowapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import consumer.deserialization.AvroDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import scala.collection.JavaConverters;
import uk.gov.companieshouse.itemgroupworkflowapi.kafka.ItemOrderedCertifiedCopyFactory;
import uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtils;
import uk.gov.companieshouse.itemgroupworkflowapi.model.DeliveryDetails;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Item;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemCostProductType;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemCosts;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemDescriptionIdentifier;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroupData;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemKind;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemLinks;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Links;
import uk.gov.companieshouse.itemgroupworkflowapi.repository.ItemGroupsRepository;
import uk.gov.companieshouse.itemgroupworkflowapi.service.IdGenerator;
import uk.gov.companieshouse.itemorderedcertifiedcopy.ItemOrderedCertifiedCopy;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.Constants.TOPIC_NAME;

/**
 * Integration tests the {@link uk.gov.companieshouse.itemgroupworkflowapi.controller.ItemGroupController} class's
 * handling of the create item group POST request only.
 */
@SpringBootTest
@EmbeddedKafka
@AutoConfigureMockMvc
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@ComponentScan("uk.gov.companieshouse.itemgroupworkflowapi")
class ItemGroupControllerCreateItemGroupIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger("ItemGroupControllerIntegrationTest");

    private static final String EXPECTED_ORDER_NUMBER = "123456";
    private static final String VALID_DELIVERY_COMPANY_NAME = "Delivery Test Company";
    private static final String VALID_ITEM_COMPANY_NAME = "Item Test Company";
    private static final String VALID_COMPANY_NUMBER = "IG-12345-67890";
    public static final String REQUEST_ID_HEADER_NAME = "X-Request-ID";

    private static final int MESSAGE_WAIT_TIMEOUT_SECONDS = 5;

    private static final ItemOrderedCertifiedCopy EXPECTED_CERTIFIED_COPY = ItemOrderedCertifiedCopy.newBuilder()
            .setOrderNumber("123456")
            .setItemId("111-222-333")
            .setGroupItem("/item-groups/IG-123456-123456/items/111-222-333")
            .setCompanyName("Item Test Company")
            .setCompanyNumber("IG-12345-67890")
            .setFilingHistoryDescription("appoint-person-director-company-with-name-date")
            .setFilingHistoryId("OTYyMTM3NjgxOGFkaXF6a2N4")
            .setFilingHistoryType("AP01")
            .setFilingHistoryDescriptionValues(
                    Map.of("appointment_date", "2023-05-01",
                           "officer_name", "Mr Tom Sunburn")
            )
            .build();

    @Configuration
    static class Config {

        @Bean
        public ConsumerFactory<String, ItemOrderedCertifiedCopy> consumerFactory(
                @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
            return new DefaultKafkaConsumerFactory<>(
                    Map.of(
                            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                            ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class,
                            ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, AvroDeserializer.class,
                            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"),
                    new StringDeserializer(),
                    new ErrorHandlingDeserializer<>(new AvroDeserializer<>(ItemOrderedCertifiedCopy.class)));
        }

        @Bean
        public ConcurrentKafkaListenerContainerFactory<String, ItemOrderedCertifiedCopy> kafkaListenerContainerFactory(
                ConsumerFactory<String, ItemOrderedCertifiedCopy> consumerFactory) {
            ConcurrentKafkaListenerContainerFactory<String, ItemOrderedCertifiedCopy> factory =
                    new ConcurrentKafkaListenerContainerFactory<>();
            factory.setConsumerFactory(consumerFactory);
            factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
            return factory;
        }

        static class TestItemOrderedCertifiedCopyFactory extends ItemOrderedCertifiedCopyFactory {

            public TestItemOrderedCertifiedCopyFactory(LoggingUtils loggingUtils) {
                super(loggingUtils);
            }

            @Override
            protected Map getFilingHistoryDocument(Item item) {
                // TODO DCAC-68 Is is safe to assume we can always get FH details from the 1st filing history document?
                final var options = item.getItemOptions();

                // TODO DCAC-68: This Scala weirdness only seems to arise in our Spring Boot integration tests.
                // This problem might disappear if we use typed collections to create the request body?
                final Map filingHistoryDocument;
                if (options.get("filing_history_documents") instanceof scala.collection.immutable.List) {
                    getLogger().info("Scala classes detected in the filing history documents.");
                    filingHistoryDocument = buildFilingHistoryDocumentFromOptionsScalaDocuments(options);
                } else {
                    getLogger().info("Scala classes not detected in the filing history documents.");
                    filingHistoryDocument = (Map) ((List) options.get("filing_history_documents")).get(0);
                }

                // TODO DCAC-68 Structured logging, or remove this.
                getLogger().info("filingHistoryDocument = " + filingHistoryDocument);
                return filingHistoryDocument;
            }

            private Map buildFilingHistoryDocumentFromOptionsScalaDocuments(final Map options) {
                final var scalaFilingHistoryDocuments =
                        (scala.collection.immutable.List) options.get("filing_history_documents");
                final var javaFilingHistoryDocuments = JavaConverters.asJava(scalaFilingHistoryDocuments);
                final var scalaFilingHistoryDocument = (scala.collection.immutable.Map) javaFilingHistoryDocuments.get(0);
                final var immutableJavaFilingHistoryDocument = JavaConverters.asJava(scalaFilingHistoryDocument);
                final var scalaDescriptionValues =
                        (scala.collection.immutable.Map)
                                immutableJavaFilingHistoryDocument.get("filing_history_description_values");
                final var javaDescriptionValues = JavaConverters.asJava(scalaDescriptionValues);
                // The put operation requires that the map in question is mutable.
                final var filingHistoryDocument = new HashMap(immutableJavaFilingHistoryDocument);
                filingHistoryDocument.put("filing_history_description_values", javaDescriptionValues);
                return filingHistoryDocument;
            }
        }

        @Bean
        @Primary
        public ItemOrderedCertifiedCopyFactory getItemOrderedCertifiedCopyFactory(final LoggingUtils loggingUtils) {
            return new TestItemOrderedCertifiedCopyFactory(loggingUtils);
        }

    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ItemGroupsRepository repository;

    @MockBean
    private IdGenerator idGenerator;

    private CountDownLatch messageReceivedLatch;
    private ItemOrderedCertifiedCopy messageReceived;

    @BeforeEach
    void setUp() {
        resetMessageReceivedLatch();
        when(idGenerator.generateId()).thenReturn("IG-123456-123456");
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();
        messageReceived = null;
    }

    @Test
    @DisplayName("Create successful itemGroup - 201 Created")
    void createItemGroupSuccessful201Created() throws Exception {

        // Given
        final ItemGroupData newItemGroupData = createValidNewItemGroupData();

        // When and Then
        mockMvc.perform(post("/item-groups" )
                        .header(REQUEST_ID_HEADER_NAME, "12345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newItemGroupData)))
                .andExpect(status().isCreated())
                .andDo(MockMvcResultHandlers.print());

        verifyExpectedMessageIsReceived();
    }

    @Test
    @DisplayName("Create itemGroup unsuccessful - 400 Bad Request")
    void createItemGroupUnsuccessful400BadRequest() throws Exception {

        // Given
        final ItemGroupData newItemGroupData = createInvalidNewItemGroupData();

        // When and Then
        mockMvc.perform(post("/item-groups" )
                        .header(REQUEST_ID_HEADER_NAME, "12345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newItemGroupData)))
                .andExpect(status().isBadRequest())
                .andDo(MockMvcResultHandlers.print());

        verifyWhetherMessageIsReceived(false);
    }

    @Test
    @DisplayName("create duplicate itemGroup fails - 409 Conflict")
    void createDuplicateItemGroupUnsuccessful409Conflict() throws Exception {

        // Given
        final ItemGroupData newItemGroupData = createValidNewItemGroupData();

        // Create item group and get success status.
        mockMvc.perform(post("/item-groups" )
                        .header(REQUEST_ID_HEADER_NAME, "12345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newItemGroupData)))
                .andExpect(status().isCreated())
                .andDo(MockMvcResultHandlers.print());

        verifyWhetherMessageIsReceived(true);
        resetMessageReceivedLatch();

        // Attempt to create the same item group and get failure status, 409 - CONFLICT.
        mockMvc.perform(post("/item-groups" )
                        .header(REQUEST_ID_HEADER_NAME, "12345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newItemGroupData)))
                .andExpect(status().isConflict())
                .andDo(MockMvcResultHandlers.print());

        verifyWhetherMessageIsReceived(false);
    }

    /**
     * Factory method that produces a DTO for a valid create item group payload.
     *
     * @return a valid item DTO
     */
    private ItemGroupData createValidNewItemGroupData() {
        final ItemGroupData newItemGroupData = new ItemGroupData();
        newItemGroupData.setOrderNumber(EXPECTED_ORDER_NUMBER);

        DeliveryDetails deliveryDetails = new DeliveryDetails();
        deliveryDetails.setCompanyName(VALID_DELIVERY_COMPANY_NAME);
        newItemGroupData.setDeliveryDetails(deliveryDetails);

        ItemCosts itemCost = new ItemCosts();
        itemCost.setProductType(ItemCostProductType.CERTIFIED_COPY_INCORPORATION.toString());

        List<ItemCosts> itemCosts = new ArrayList<>();
        itemCosts.add(itemCost);

        Links links = new Links();
        links.setOrder(EXPECTED_ORDER_NUMBER);
        links.setSelf("/orderable/certificates/mycert-123");
        newItemGroupData.setLinks(links);

        Item item = new Item();
        item.setCompanyNumber(VALID_COMPANY_NUMBER);
        item.setId("111-222-333");
        item.setCompanyName(VALID_ITEM_COMPANY_NAME);
        item.setDescriptionIdentifier(ItemDescriptionIdentifier.CERTIFIED_COPY.toString());
        item.setKind(ItemKind.ITEM_CERTIFIED_COPY.toString());
        item.setItemCosts(itemCosts);

        ItemLinks itemLinks = new ItemLinks();
        itemLinks.setOriginalItem("/orderable/certificates/mycert-123");
        item.setLinks(itemLinks);

        item.setItemOptions(
            Map.of("filing_history_documents",
                List.of(
                    Map.of(
                        "filing_history_date", "2023-05-18",
                        "filing_history_description", "appoint-person-director-company-with-name-date",
                        "filing_history_description_values", Map.of(
                                "appointment_date", "2023-05-01",
                                "officer_name", "Mr Tom Sunburn"
                        ),
                        "filing_history_id", "OTYyMTM3NjgxOGFkaXF6a2N4",
                        "filing_history_type", "AP01",
                        "filing_history_cost", "50"
                    )
                ),
                "delivery_method", "collection",
                "delivery_timescale", "standard"
            )
        );

        List<Item> items = new ArrayList<>();
        items.add(item);
        newItemGroupData.setItems(items);

        return newItemGroupData;
    }

    /**
     * Factory method that produces a DTO for an invalid create item group payload - will fail validation
     *
     * @return an invalid item DTO
     */
    private ItemGroupData createInvalidNewItemGroupData() {
        final ItemGroupData newItemGroupData = new ItemGroupData();

        DeliveryDetails deliveryDetails = new DeliveryDetails();
        newItemGroupData.setDeliveryDetails(deliveryDetails);

        ItemCosts itemCost = new ItemCosts();
        List<ItemCosts> itemCosts = new ArrayList<>();
        itemCosts.add(itemCost);

        Links links = new Links();
        newItemGroupData.setLinks(links);

        Item item = new Item();
        item.setItemCosts(itemCosts);
        List<Item> items = new ArrayList<>();
        items.add(item);

        newItemGroupData.setItems(items);

        return newItemGroupData;
    }

    @KafkaListener(topics = TOPIC_NAME, groupId = "test-group")
    public void receiveMessage(final @Payload ItemOrderedCertifiedCopy message) {
        LOGGER.info("Received message: " + message);
        messageReceivedLatch.countDown();
        messageReceived = message;
    }

    private void verifyExpectedMessageIsReceived() throws InterruptedException {
        verifyWhetherMessageIsReceived(true);
        assertThat(messageReceived, is(notNullValue()));
        assertThat(Objects.deepEquals(messageReceived, EXPECTED_CERTIFIED_COPY), is(true));
    }

    private void verifyWhetherMessageIsReceived(final boolean messageIsReceived) throws InterruptedException {
        LOGGER.info("Waiting to receive message for up to " + MESSAGE_WAIT_TIMEOUT_SECONDS + " seconds.");
        final var received = messageReceivedLatch.await(MESSAGE_WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertThat(received, is(messageIsReceived));
    }

    private void resetMessageReceivedLatch() {
        messageReceivedLatch = new CountDownLatch(1);
    }

}
