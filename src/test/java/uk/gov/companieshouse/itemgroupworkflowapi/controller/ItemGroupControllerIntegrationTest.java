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
import uk.gov.companieshouse.itemgroupworkflowapi.repository.ItemGroupsRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.Constants.REQUEST_ID_HEADER_NAME;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.PatchMediaType.APPLICATION_MERGE_PATCH;

@AutoConfigureMockMvc
@SpringBootTest
class ItemGroupControllerIntegrationTest {

    private static final String ITEM_GROUP_ID = "IG-922016-860413";
    private static final String PATCH_ITEM_URI = "/item-groups/" + ITEM_GROUP_ID + "/items/111-222-333";

    private static final String PATCH_ITEM_BODY = "{\n" +
            "    \"digital_document_location\": " +
            "\"s3://document-api-images-cidev/docs/--EdB7fbldt5oujK6Nz7jZ3hGj_x6vW8Q_2gQTyjWBM/application-pdf\",\n" +
            "    \"status\": \"satisfied\"\n" +
            "}";

    private static final String PATCH_ITEM_BODY_WITHOUT_STATUS = "{\n" +
            "    \"digital_document_location\": " +
            "\"s3://document-api-images-cidev/docs/--EdB7fbldt5oujK6Nz7jZ3hGj_x6vW8Q_2gQTyjWBM/application-pdf\"\n" +
            "}";

    private static final String PATCH_ITEM_BODY_WITH_BAD_STATUS = "{\n" +
            "    \"digital_document_location\": " +
            "\"s3://document-api-images-cidev/docs/--EdB7fbldt5oujK6Nz7jZ3hGj_x6vW8Q_2gQTyjWBM/application-pdf\",\n" +
            "    \"status\": \"bad\"\n" +
            "}";

    private static final String REQUEST_ID = "WmuRTepX70C635NKm5rbYTciSsOR";

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

        mockMvc.perform(patch(PATCH_ITEM_URI)
                        .header(REQUEST_ID_HEADER_NAME, REQUEST_ID)
                        .contentType(APPLICATION_MERGE_PATCH)
                        .content(PATCH_ITEM_BODY))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("patch item rejects request missing the status field")
    void patchItemRejectsRequestWithoutStatus() throws Exception {
        mockMvc.perform(patch(PATCH_ITEM_URI)
                        .header(REQUEST_ID_HEADER_NAME, REQUEST_ID)
                        .contentType(APPLICATION_MERGE_PATCH)
                        .content(PATCH_ITEM_BODY_WITHOUT_STATUS))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("patch item rejects request with an invalid status field value")
    void patchItemRejectsRequestWithInvalidStatus() throws Exception {
        mockMvc.perform(patch(PATCH_ITEM_URI)
                        .header(REQUEST_ID_HEADER_NAME, REQUEST_ID)
                        .contentType(APPLICATION_MERGE_PATCH)
                        .content(PATCH_ITEM_BODY_WITH_BAD_STATUS))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    private void setUpItemGroup() throws IOException {
        final String itemGroup =
                new String(Files.readAllBytes(Paths.get("src/test/resources/testdata/item_group.json")));
        mongoTemplate.insert(Document.parse(itemGroup), "item_groups");
    }

}
