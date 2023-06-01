package uk.gov.companieshouse.itemgroupworkflowapi.controller;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.Constants.REQUEST_ID_HEADER_NAME;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.PatchMediaType.APPLICATION_MERGE_PATCH;

@AutoConfigureMockMvc
@SpringBootTest
class ItemGroupControllerIntegrationTest {

    // TODO DCAC-78 A more convincing URI?
    private static final String PATCH_ITEM_URI = "/item-groups/1/items/1";

    private static final String PATCH_ITEM_BODY = "{\n" +
            "    \"digital_document_location\": \"s3://document-api-images-cidev/docs/--EdB7fbldt5oujK6Nz7jZ3hGj_x6vW8Q_2gQTyjWBM/application-pdf\",\n" +
            "    \"status\": \"satisfied\"\n" +
            "}";

    private static final String REQUEST_ID = "WmuRTepX70C635NKm5rbYTciSsOR";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void patchItemSuccessfully() throws Exception {
        mockMvc.perform(patch(PATCH_ITEM_URI)
                        .header(REQUEST_ID_HEADER_NAME, REQUEST_ID)
                        .contentType(APPLICATION_MERGE_PATCH)
                        .content(PATCH_ITEM_BODY))
                .andExpect(status().isOk())
                .andDo(print());
    }

}
