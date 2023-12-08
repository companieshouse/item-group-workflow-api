package uk.gov.companieshouse.itemgroupworkflowapi.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.itemgroupworkflowapi.service.ItemStatusPropagationService.ITEM_STATUS_UPDATED_URL;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.PatchMediaType.APPLICATION_MERGE_PATCH;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.TestConstants.ERIC_AUTHORISED_ROLES_HEADER_NAME;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.TestConstants.ERIC_AUTHORISED_ROLES_HEADER_VALUE;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.TestConstants.ERIC_IDENTITY_HEADER_NAME;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.TestConstants.ERIC_IDENTITY_HEADER_VALUE;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.TestConstants.ERIC_IDENTITY_TYPE_HEADER_NAME;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.TestConstants.ERIC_IDENTITY_TYPE_HEADER_VALUE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.itemgroupprocessed.ItemGroupProcessed;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Item;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroup;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemLinks;
import uk.gov.companieshouse.itemgroupworkflowapi.model.TimestampedEntity;
import uk.gov.companieshouse.itemgroupworkflowapi.repository.ItemGroupsRepository;
import uk.gov.companieshouse.itemgroupworkflowapi.util.TimestampedEntityVerifier;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

/**
 * Integration tests the {@link uk.gov.companieshouse.itemgroupworkflowapi.controller.ItemGroupController} class's
 * handling of the PATCH item request only.
 */
@SpringBootTest(properties = "chs.kafka.api.url=http://localhost:${wiremock.server.port}")
@EmbeddedKafka
@AutoConfigureMockMvc
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@ComponentScan("uk.gov.companieshouse.itemgroupworkflowapi")
@AutoConfigureWireMock(port = 0)
class ItemGroupControllerPatchItemIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        "ItemGroupControllerPatchItemIntegrationTest");

    public static final String REQUEST_ID_HEADER_NAME = "X-Request-ID";

    private static final String ITEM_GROUP_ID = "IG-922016-860413";
    private static final String ITEM_ID = "111-222-333";
 //private static final String ITEM_ID = "CCD-768116-517930";
    private static final String UNKNOWN_ITEM_ID = "111-222-4444";
    private static final String PATCH_ITEM_URI = "/item-groups/" + ITEM_GROUP_ID + "/items/" + ITEM_ID;
    private static final String PATCH_UNKNOWN_ITEM_URI = "/item-groups/" + ITEM_GROUP_ID + "/items/" + UNKNOWN_ITEM_ID;
    private static final String REQUEST_ID = "WmuRTepX70C635NKm5rbYTciSsOR";

    private static final String EXPECTED_DIGITAL_DOCUMENT_LOCATION =
            "s3://document-api-images-cidev/docs/--EdB7fbldt5oujK6Nz7jZ3hGj_x6vW8Q_2gQTyjWBM/application-pdf";
    private static final String EXPECTED_STATUS = "satisfied";

    private static final String ITEM_GROUP_PROCESSED_TOPIC = "item-group-processed";

    private static final int MESSAGE_WAIT_TIMEOUT_SECONDS = 10;


    private static final String ORDER_NUMBER = "ORD-065216-517934";
    private static final String GROUP_ITEM = "/item-groups/IG-256616-866507/items/CCD-768116-517930";
    private static final String STATUS = "satisfied";
    private static final String DIGITAL_DOCUMENT_LOCATION =
        "s3://document-api-images-cidev/docs/--EdB7fbldt5oujK6Nz7jZ3hGj_x6vW8Q_2gQTyjWBM/application-pdf";

    private static final Item ITEM = new Item();
    private static final ItemGroup ITEM_GROUP = new ItemGroup();
    private static final ItemGroupProcessed EXPECTED_COMPLETE_MESSAGE;
    private static final ItemGroupProcessed EXPECTED_INCOMPLETE_MESSAGE;

    static {
        ITEM_GROUP.getData().setOrderNumber(ORDER_NUMBER);
        final ItemLinks links = new ItemLinks();
        links.setSelf(GROUP_ITEM);
        ITEM.setLinks(links);
        ITEM.setId(ITEM_ID);
        ITEM.setStatus(STATUS);
        EXPECTED_INCOMPLETE_MESSAGE = ItemGroupProcessed.newBuilder()
            .setOrderNumber(ORDER_NUMBER)
            .setGroupItem(GROUP_ITEM)
            .setItem(buildAvroItem(ITEM))
            .build();
        ITEM.setDigitalDocumentLocation(DIGITAL_DOCUMENT_LOCATION);
        EXPECTED_COMPLETE_MESSAGE = ItemGroupProcessed.newBuilder()
            .setOrderNumber(ORDER_NUMBER)
            .setGroupItem(GROUP_ITEM)
            .setItem(buildAvroItem(ITEM))
            .build();
    }


    private static final class ItemGroupTimeStampedEntity implements TimestampedEntity {

        private final ItemGroup group;

        private ItemGroupTimeStampedEntity(ItemGroup group) {
            this.group = group;
        }

        @Override
        public LocalDateTime getCreatedAt() {
            return group.getCreatedAt();
        }

        @Override
        public LocalDateTime getUpdatedAt() {
            return group.getUpdatedAt();
        }
    }

    private static final class ItemTimestampedEntity implements TimestampedEntity {

        private final Item item;
        private final ItemGroup group;

        private ItemTimestampedEntity(Item item, ItemGroup group) {
            this.item = item;
            this.group = group;
        }

        @Override
        public LocalDateTime getCreatedAt() {
            // use item group's creation time as there is none on the item
            return group.getCreatedAt();
        }

        @Override
        public LocalDateTime getUpdatedAt() {
            return item.getUpdatedAt();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemGroupsRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private CountDownLatch messageReceivedLatch;
    private ItemGroupProcessed messageReceived;

    @BeforeEach
    void setUp() {
        messageReceivedLatch = new CountDownLatch(1);
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("patch item handles valid request successfully")
    void patchItemSuccessfully() throws Exception {

        setUpItemGroup();

        final var timestamps = new TimestampedEntityVerifier();
        timestamps.start();

        givenThat(post(urlEqualTo(ITEM_STATUS_UPDATED_URL))
            .willReturn(aResponse()
                .withStatus(201)));

        mockMvc.perform(patch(PATCH_ITEM_URI)
                        .header(REQUEST_ID_HEADER_NAME, REQUEST_ID)
                        .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_TYPE_HEADER_VALUE)
                        .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_HEADER_VALUE)
                        .header(ERIC_AUTHORISED_ROLES_HEADER_NAME, ERIC_AUTHORISED_ROLES_HEADER_VALUE)
                        .contentType(APPLICATION_MERGE_PATCH)
                        .content(getJsonFromFile("patch_item_body")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.digital_document_location", is(EXPECTED_DIGITAL_DOCUMENT_LOCATION)))
                .andExpect(jsonPath("$.status", is(EXPECTED_STATUS)))
                .andDo(print());

        timestamps.end();

        final var optionalGroup = repository.findById(ITEM_GROUP_ID);
        assertThat(optionalGroup.isPresent(), is(true));
        final var retrievedGroup = optionalGroup.get();
        final var retrievedItem = retrievedGroup.getData().getItems().get(0);
        assertThat(retrievedItem.getDigitalDocumentLocation(), is(EXPECTED_DIGITAL_DOCUMENT_LOCATION));
        assertThat(retrievedItem.getStatus(), is(EXPECTED_STATUS));
        timestamps.verifyUpdatedAtTimestampWithinExecutionInterval(new ItemGroupTimeStampedEntity(retrievedGroup));
        timestamps.verifyUpdatedAtTimestampWithinExecutionInterval(
                new ItemTimestampedEntity(retrievedItem, retrievedGroup));

        verify(postRequestedFor(urlEqualTo(ITEM_STATUS_UPDATED_URL)));
        verifyExpectedMessageIsReceived(EXPECTED_COMPLETE_MESSAGE);
    }

    @Test
    @DisplayName("patch item rejects request missing the status field")
    void patchItemRejectsRequestWithoutStatus() throws Exception {
        mockMvc.perform(patch(PATCH_ITEM_URI)
                        .header(REQUEST_ID_HEADER_NAME, REQUEST_ID)
                        .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_TYPE_HEADER_VALUE)
                        .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_HEADER_VALUE)
                        .header(ERIC_AUTHORISED_ROLES_HEADER_NAME, ERIC_AUTHORISED_ROLES_HEADER_VALUE)
                        .contentType(APPLICATION_MERGE_PATCH)
                        .content(getJsonFromFile("patch_item_body_without_status")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("status: must not be null")))
                .andDo(print());
    }

    @Test
    @DisplayName("patch item rejects request with an invalid status field value")
    void patchItemRejectsRequestWithInvalidStatus() throws Exception {
        mockMvc.perform(patch(PATCH_ITEM_URI)
                        .header(REQUEST_ID_HEADER_NAME, REQUEST_ID)
                        .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_TYPE_HEADER_VALUE)
                        .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_HEADER_VALUE)
                        .header(ERIC_AUTHORISED_ROLES_HEADER_NAME, ERIC_AUTHORISED_ROLES_HEADER_VALUE)
                        .contentType(APPLICATION_MERGE_PATCH)
                        .content(getJsonFromFile("patch_item_body_with_bad_status")))
                .andExpect(status().isBadRequest())
                .andExpect(
                        content().string(containsString(
                                "status: must be one of [pending, processing, satisfied, cancelled, failed]")))
                .andDo(print());
    }

    @Test
    @DisplayName("patch item patches without the digital_document_location field")
    void patchItemPatchesWithoutDocumentLocation() throws Exception {

        setUpItemGroup();

        givenThat(post(urlEqualTo(ITEM_STATUS_UPDATED_URL))
            .willReturn(aResponse()
                .withStatus(201)));

        mockMvc.perform(patch(PATCH_ITEM_URI)
                        .header(REQUEST_ID_HEADER_NAME, REQUEST_ID)
                        .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_TYPE_HEADER_VALUE)
                        .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_HEADER_VALUE)
                        .header(ERIC_AUTHORISED_ROLES_HEADER_NAME, ERIC_AUTHORISED_ROLES_HEADER_VALUE)
                        .contentType(APPLICATION_MERGE_PATCH)
                        .content(getJsonFromFile("patch_item_body_without_document_location")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.digital_document_location").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.status", is(EXPECTED_STATUS)))
                .andDo(print());

        verify(postRequestedFor(urlEqualTo(ITEM_STATUS_UPDATED_URL)));
        verifyExpectedMessageIsReceived(EXPECTED_INCOMPLETE_MESSAGE);
    }

    @Test
    @DisplayName("patch item rejects request with an invalid digital_document_location field value")
    void patchItemRejectsRequestWithInvalidDocumentLocation() throws Exception {
        mockMvc.perform(patch(PATCH_ITEM_URI)
                        .header(REQUEST_ID_HEADER_NAME, REQUEST_ID)
                        .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_TYPE_HEADER_VALUE)
                        .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_HEADER_VALUE)
                        .header(ERIC_AUTHORISED_ROLES_HEADER_NAME, ERIC_AUTHORISED_ROLES_HEADER_VALUE)
                        .contentType(APPLICATION_MERGE_PATCH)
                        .content(getJsonFromFile("patch_item_body_with_invalid_document_location")))
                .andExpect(status().isBadRequest())
                .andExpect(
                        content().string(containsString(
                                "digital_document_location: " +
                                        "s3:// document-api-images-cidev/docs/" +
                                        "--EdB7fbldt5oujK6Nz7jZ3hGj_x6vW8Q_2gQTyjWBM/application-pdf " +
                                        "is not a valid URI.")))
                .andDo(print());
    }

    @Test
    @DisplayName("patch item reports item not found when it cannot find it")
    void patchItemReportsNotFoundWhenCannotFindItem() throws Exception {
        mockMvc.perform(patch(PATCH_UNKNOWN_ITEM_URI)
                        .header(REQUEST_ID_HEADER_NAME, REQUEST_ID)
                        .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_TYPE_HEADER_VALUE)
                        .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_HEADER_VALUE)
                        .header(ERIC_AUTHORISED_ROLES_HEADER_NAME, ERIC_AUTHORISED_ROLES_HEADER_VALUE)
                        .contentType(APPLICATION_MERGE_PATCH)
                        .content(getJsonFromFile("patch_item_body")))
                .andExpect(status().isNotFound())
                .andExpect(content().string("")) // NOTE actual response does have meaningful content
                .andDo(print());
    }

    @Test
    @DisplayName("patch item rejects request without a Content-Type=application/merge-patch+json header value")
    void patchItemRejectsRequestWithoutApplicationMergePatchContentType() throws Exception {
        mockMvc.perform(patch(PATCH_ITEM_URI)
                        .header(REQUEST_ID_HEADER_NAME, REQUEST_ID)
                        .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_TYPE_HEADER_VALUE)
                        .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_HEADER_VALUE)
                        .header(ERIC_AUTHORISED_ROLES_HEADER_NAME, ERIC_AUTHORISED_ROLES_HEADER_VALUE)
                        .content(getJsonFromFile("patch_item_body")))
                .andExpect(status().isUnsupportedMediaType())
                .andDo(print());
    }

    @Test
    @DisplayName("patch item responds with error where issue encountered propagating status update")
    void patchItemPatchesFailsShouldStatusUpdatePropagationFail() throws Exception {

        setUpItemGroup();

        givenThat(post(urlEqualTo(ITEM_STATUS_UPDATED_URL))
            .willReturn(aResponse()
                .withStatus(404)));

        mockMvc.perform(patch(PATCH_ITEM_URI)
                .header(REQUEST_ID_HEADER_NAME, REQUEST_ID)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_TYPE_HEADER_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_HEADER_VALUE)
                .header(ERIC_AUTHORISED_ROLES_HEADER_NAME, ERIC_AUTHORISED_ROLES_HEADER_VALUE)
                .contentType(APPLICATION_MERGE_PATCH)
                .content(getJsonFromFile("patch_item_body")))
            .andExpect(status().is5xxServerError())
            .andExpect(content().string(
                "Error in item-group-workflow-api: Item status update propagation FAILED for order number"
                    + " ORD-065216-517934, group item /item-groups/IG-922016-860413/items/111-222-333, caught"
                    + " RestClientException with message 404 Not Found: [no body]."))
            .andDo(print());

        verify(postRequestedFor(urlEqualTo(ITEM_STATUS_UPDATED_URL)));
    }

    private void setUpItemGroup() throws IOException {
        final String itemGroup = getJsonFromFile("item_group");
        mongoTemplate.insert(Document.parse(itemGroup), "item_groups");
    }

    private String getJsonFromFile(final String fileBasename) throws IOException {
        return new String(Files.readAllBytes(Paths.get("src/test/resources/testdata/" + fileBasename + ".json")));
    }


    @KafkaListener(topics = ITEM_GROUP_PROCESSED_TOPIC, groupId = "test-group")
    public void receiveMessage(final @Payload ItemGroupProcessed message) {
        LOGGER.info("Received message: " + message);
        messageReceived = message;
        messageReceivedLatch.countDown();
    }

    private void verifyExpectedMessageIsReceived(final ItemGroupProcessed expectedMessage)
        throws InterruptedException {
        verifyWhetherMessageIsReceived(true);
        assertThat(messageReceived, is(notNullValue()));
        assertThat(Objects.deepEquals(messageReceived, expectedMessage),
            is(true));
    }

    private void verifyWhetherMessageIsReceived(final boolean messageIsReceived)
        throws InterruptedException {
        LOGGER.info(
            "Waiting to receive message for up to " + MESSAGE_WAIT_TIMEOUT_SECONDS + " seconds.");
        final var received = messageReceivedLatch.await(MESSAGE_WAIT_TIMEOUT_SECONDS,
            TimeUnit.SECONDS);
        assertThat(received, is(messageIsReceived));
    }

    private static uk.gov.companieshouse.itemgroupprocessed.Item buildAvroItem(final Item item) {
        return uk.gov.companieshouse.itemgroupprocessed.Item.newBuilder()
            .setId(item.getId())
            .setStatus(item.getStatus())
            .setDigitalDocumentLocation(item.getDigitalDocumentLocation())
            .build();
    }

}
