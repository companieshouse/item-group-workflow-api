package uk.gov.companieshouse.itemgroupworkflowapi.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.hamcrest.core.StringContains.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.itemgroupworkflowapi.service.ItemStatusPropagationService.ITEM_STATUS_UPDATED_URL;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.PatchMediaType.APPLICATION_MERGE_PATCH;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.TestConstants.ERIC_AUTHORISED_ROLES_HEADER_NAME;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.TestConstants.ERIC_AUTHORISED_ROLES_HEADER_VALUE;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.TestConstants.ERIC_IDENTITY_HEADER_NAME;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.TestConstants.ERIC_IDENTITY_HEADER_VALUE;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.TestConstants.ERIC_IDENTITY_TYPE_HEADER_NAME;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.TestConstants.ERIC_IDENTITY_TYPE_HEADER_VALUE;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.TestConstants.ITEM_ID;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.itemgroupworkflowapi.config.AbstractMongoConfig;
import uk.gov.companieshouse.itemgroupworkflowapi.repository.ItemGroupsRepository;

/**
 * Integration tests the
 * {@link uk.gov.companieshouse.itemgroupworkflowapi.controller.ItemGroupController} class's
 * handling of the PATCH item request only.
 */
@Testcontainers
@SpringBootTest(properties = "chs.kafka.api.url=http://localhost:${wiremock.server.port}")
@EmbeddedKafka
@AutoConfigureMockMvc
@ComponentScan("uk.gov.companieshouse.itemgroupworkflowapi")
@AutoConfigureWireMock(port = 11419)
class ItemGroupControllerPatchItemNegativeIntegrationTest extends AbstractMongoConfig {

    public static final String REQUEST_ID_HEADER_NAME = "X-Request-ID";

    private static final String ITEM_GROUP_ID = "IG-922016-860413";

    private static final String UNKNOWN_ITEM_ID = "111-222-4444";
    private static final String PATCH_ITEM_URI =
        "/item-groups/" + ITEM_GROUP_ID + "/items/" + ITEM_ID;
    private static final String PATCH_UNKNOWN_ITEM_URI =
        "/item-groups/" + ITEM_GROUP_ID + "/items/" + UNKNOWN_ITEM_ID;
    private static final String REQUEST_ID = "WmuRTepX70C635NKm5rbYTciSsOR";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemGroupsRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeAll
    static void setup() {
        mongoDBContainer.start();
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();
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
                    + " ORD-065216-517934, group item /item-groups/IG-922016-860413/items/CCD-768116-517930, caught"
                    + " RestClientException with message 404 Not Found on POST request for \"http://localhost:11419/private/item-group-processed-send\": [no body]."))
            .andDo(print());

        verify(postRequestedFor(urlEqualTo(ITEM_STATUS_UPDATED_URL)));
    }

    private void setUpItemGroup() throws IOException {
        final String itemGroup = getJsonFromFile("item_group");
        mongoTemplate.insert(Document.parse(itemGroup), "item_groups");
    }

    private String getJsonFromFile(final String fileBasename) throws IOException {
        return new String(
            Files.readAllBytes(Paths.get("src/test/resources/testdata/" + fileBasename + ".json")));
    }

}
