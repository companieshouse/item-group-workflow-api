package uk.gov.companieshouse.itemgroupworkflowapi.controller;


import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Item;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroup;
import uk.gov.companieshouse.itemgroupworkflowapi.model.TimestampedEntity;
import uk.gov.companieshouse.itemgroupworkflowapi.repository.ItemGroupsRepository;
import uk.gov.companieshouse.itemgroupworkflowapi.util.TimestampedEntityVerifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.Constants.REQUEST_ID_HEADER_NAME;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.PatchMediaType.APPLICATION_MERGE_PATCH;

@AutoConfigureMockMvc
@SpringBootTest
class ItemGroupControllerIntegrationTest {

    private static final String ITEM_GROUP_ID = "IG-922016-860413";
    private static final String PATCH_ITEM_URI = "/item-groups/" + ITEM_GROUP_ID + "/items/111-222-333";
    private static final String REQUEST_ID = "WmuRTepX70C635NKm5rbYTciSsOR";

    private static final String EXPECTED_DIGITAL_DOCUMENT_LOCATION =
            "s3://document-api-images-cidev/docs/--EdB7fbldt5oujK6Nz7jZ3hGj_x6vW8Q_2gQTyjWBM/application-pdf";
    private static final String EXPECTED_STATUS = "satisfied";

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

    @AfterEach
    void tearDown() {
        repository.findById(ITEM_GROUP_ID).ifPresent(repository::delete);
    }
    
    @Test
    @DisplayName("patch item handles valid request successfully")
    void patchItemSuccessfully() throws Exception {

        setUpItemGroup();

        final var timestamps = new TimestampedEntityVerifier();
        timestamps.start();

        mockMvc.perform(patch(PATCH_ITEM_URI)
                        .header(REQUEST_ID_HEADER_NAME, REQUEST_ID)
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
        timestamps.verifyUpdatedAtTimestampWithinExecutionInterval(new ItemTimestampedEntity(retrievedItem, retrievedGroup));
    }

    @Test
    @DisplayName("patch item rejects request missing the status field")
    void patchItemRejectsRequestWithoutStatus() throws Exception {
        mockMvc.perform(patch(PATCH_ITEM_URI)
                        .header(REQUEST_ID_HEADER_NAME, REQUEST_ID)
                        .contentType(APPLICATION_MERGE_PATCH)
                        .content(getJsonFromFile("patch_item_body_without_status")))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("patch item rejects request with an invalid status field value")
    void patchItemRejectsRequestWithInvalidStatus() throws Exception {
        mockMvc.perform(patch(PATCH_ITEM_URI)
                        .header(REQUEST_ID_HEADER_NAME, REQUEST_ID)
                        .contentType(APPLICATION_MERGE_PATCH)
                        .content(getJsonFromFile("patch_item_body_with_bad_status")))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    private void setUpItemGroup() throws IOException {
        final String itemGroup = getJsonFromFile("item_group");
        mongoTemplate.insert(Document.parse(itemGroup), "item_groups");
    }

    private String getJsonFromFile(final String fileBasename) throws IOException {
        return new String(Files.readAllBytes(Paths.get("src/test/resources/testdata/" + fileBasename + ".json")));
    }

}
