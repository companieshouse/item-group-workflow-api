package uk.gov.companieshouse.itemgroupworkflowapi.integration;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.TestConstants.CERTIFIED_COPY_ITEM_OPTIONS;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.TestConstants.ERIC_AUTHORISED_ROLES_HEADER_NAME;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.TestConstants.ERIC_AUTHORISED_ROLES_HEADER_VALUE;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.TestConstants.ERIC_IDENTITY_HEADER_NAME;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.TestConstants.ERIC_IDENTITY_HEADER_VALUE;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.TestConstants.ERIC_IDENTITY_TYPE_HEADER_NAME;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.TestConstants.ERIC_IDENTITY_TYPE_HEADER_VALUE;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.TestConstants.ITEM_ORDERED_CERTIFIED_COPY_TOPIC;

import com.fasterxml.jackson.databind.ObjectMapper;
import consumer.deserialization.AvroDeserializer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
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

    private static final int MESSAGE_WAIT_TIMEOUT_SECONDS = 10;

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
        resetMessageReceivedLatch(1);
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
                        .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_TYPE_HEADER_VALUE)
                        .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_HEADER_VALUE)
                        .header(ERIC_AUTHORISED_ROLES_HEADER_NAME, ERIC_AUTHORISED_ROLES_HEADER_VALUE)
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
                        .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_TYPE_HEADER_VALUE)
                        .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_HEADER_VALUE)
                        .header(ERIC_AUTHORISED_ROLES_HEADER_NAME, ERIC_AUTHORISED_ROLES_HEADER_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newItemGroupData)))
                .andExpect(status().isBadRequest())
                .andDo(MockMvcResultHandlers.print());

        verifyWhetherMessageIsReceived(false);
    }


    @Test
    @DisplayName("Create item group with missing item options is rejected with 400 Bad Request")
    void createItemGroupWithoutItemOptionsIsBadRequest() throws Exception {

        // Given
        final ItemGroupData newItemGroupData = createValidNewItemGroupData();
        newItemGroupData.getItems().get(0).setItemOptions(null);

        // When and Then
        mockMvc.perform(post("/item-groups" )
                        .header(REQUEST_ID_HEADER_NAME, "12345")
                        .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_TYPE_HEADER_VALUE)
                        .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_HEADER_VALUE)
                        .header(ERIC_AUTHORISED_ROLES_HEADER_NAME, ERIC_AUTHORISED_ROLES_HEADER_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newItemGroupData)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.[0]", is("Missing item options for certified copy item 111-222-333.")))
                .andDo(MockMvcResultHandlers.print());

        verifyWhetherMessageIsReceived(false);
    }

    @Test
    @DisplayName("Create item groups sharing the same item fails - 409 Conflict")
    void createItemGroupsWithSameItemUnsuccessful409Conflict() throws Exception {

        resetMessageReceivedLatch(2); // one message for each item

        final ItemGroupData firstGroup = createItemGroupData(
            List.of(createItem("CCD-289716-962308"),
                    createItem("CCD-228916-028323")) // item shared with group 2
        );

        // Create first item group and get success status.
        mockMvc.perform(post("/item-groups" )
                        .header(REQUEST_ID_HEADER_NAME, "12345")
                        .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_TYPE_HEADER_VALUE)
                        .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_HEADER_VALUE)
                        .header(ERIC_AUTHORISED_ROLES_HEADER_NAME, ERIC_AUTHORISED_ROLES_HEADER_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(firstGroup)))
                .andExpect(status().isCreated())
                .andDo(MockMvcResultHandlers.print());

        verifyWhetherMessageIsReceived(true);
        resetMessageReceivedLatch(2); // one message for each item

        final ItemGroupData secondGroup = createItemGroupData(
            List.of(createItem("CCD-228916-028323"), // item shared with group 1
                    createItem("CCD-768116-517999"))
        );

        // Attempt to create the second item group and get failure status, 409 - CONFLICT.
        mockMvc.perform(post("/item-groups" )
                        .header(REQUEST_ID_HEADER_NAME, "12345")
                        .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_TYPE_HEADER_VALUE)
                        .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_HEADER_VALUE)
                        .header(ERIC_AUTHORISED_ROLES_HEADER_NAME, ERIC_AUTHORISED_ROLES_HEADER_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(secondGroup)))
                .andExpect(status().isConflict())
                .andDo(MockMvcResultHandlers.print());

        verifyWhetherMessageIsReceived(false);
    }

    /**
     * Factory method that produces a DTO for a valid create item group payload.
     *
     * @return a valid item group DTO
     */
    private ItemGroupData createValidNewItemGroupData() {
        return createItemGroupData(singletonList(createItem("111-222-333")));
    }

    /**
     * Factory method that produces a DTO for a valid create item group payload.
     *
     * @param items the items belonging to the item group
     * @return a valid item group DTO
     */
    private ItemGroupData createItemGroupData(final List<Item> items) {
        final ItemGroupData newItemGroupData = new ItemGroupData();
        newItemGroupData.setOrderNumber(EXPECTED_ORDER_NUMBER);

        DeliveryDetails deliveryDetails = new DeliveryDetails();
        deliveryDetails.setCompanyName(VALID_DELIVERY_COMPANY_NAME);
        newItemGroupData.setDeliveryDetails(deliveryDetails);

        Links links = new Links();
        links.setOrder(EXPECTED_ORDER_NUMBER);
        links.setSelf("/orderable/certificates/mycert-123");
        newItemGroupData.setLinks(links);
        newItemGroupData.setItems(items);

        return newItemGroupData;
    }

    /**
     * Creates (a valid) item for testing purposes
     * @param itemId the ID of the item to create
     * @return the new item
     */
    private Item createItem(final String itemId) {

        ItemCosts itemCost = new ItemCosts();
        itemCost.setProductType(ItemCostProductType.CERTIFIED_COPY_INCORPORATION.toString());

        List<ItemCosts> itemCosts = new ArrayList<>();
        itemCosts.add(itemCost);

        Item item = new Item();
        item.setCompanyNumber(VALID_COMPANY_NUMBER);
        item.setId(itemId);
        item.setCompanyName(VALID_ITEM_COMPANY_NAME);
        item.setDescriptionIdentifier(ItemDescriptionIdentifier.CERTIFIED_COPY.toString());
        item.setKind(ItemKind.ITEM_CERTIFIED_COPY.toString());
        item.setItemCosts(itemCosts);

        ItemLinks itemLinks = new ItemLinks();
        itemLinks.setOriginalItem("/orderable/certificates/mycert-123");
        item.setLinks(itemLinks);

        item.setItemOptions(CERTIFIED_COPY_ITEM_OPTIONS);

        return item;
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

    @KafkaListener(topics = ITEM_ORDERED_CERTIFIED_COPY_TOPIC, groupId = "test-group")
    public void receiveMessage(final @Payload ItemOrderedCertifiedCopy message) {
        LOGGER.info("Received message: " + message);
        messageReceived = message;
        messageReceivedLatch.countDown();
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

    private void resetMessageReceivedLatch(final int numberOfMessagesExpected) {
        messageReceivedLatch = new CountDownLatch(numberOfMessagesExpected);
    }

}
