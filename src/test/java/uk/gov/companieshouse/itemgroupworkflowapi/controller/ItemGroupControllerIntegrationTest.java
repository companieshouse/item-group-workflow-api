package uk.gov.companieshouse.itemgroupworkflowapi.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import uk.gov.companieshouse.itemgroupworkflowapi.model.DeliveryDetails;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Item;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemCostProductType;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemCosts;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemDescriptionIdentifier;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroup;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroupData;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemKind;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemLinks;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Links;
import uk.gov.companieshouse.itemgroupworkflowapi.model.TimestampedEntity;
import uk.gov.companieshouse.itemgroupworkflowapi.repository.ItemGroupsRepository;
import uk.gov.companieshouse.itemgroupworkflowapi.util.TimestampedEntityVerifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.Constants.REQUEST_ID_HEADER_NAME;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.PatchMediaType.APPLICATION_MERGE_PATCH;

/**
 * Integration tests the {@link ItemGroupController} class.
 */
@AutoConfigureMockMvc
@SpringBootTest
class ItemGroupControllerIntegrationTest {

    private static final String EXPECTED_ORDER_NUMBER = "123456";
    private static final String VALID_DELIVERY_COMPANY_NAME = "Delivery Test Company";
    private static final String VALID_ITEM_COMPANY_NAME = "Item Test Company";
    private static final String VALID_COMPANY_NUMBER = "IG-12345-67890";

    private static final String ITEM_GROUP_ID = "IG-922016-860413";
    private static final String ITEM_ID = "111-222-333";
    private static final String UNKNOWN_ITEM_ID = "111-222-4444";
    private static final String PATCH_ITEM_URI = "/item-groups/" + ITEM_GROUP_ID + "/items/" + ITEM_ID;
    private static final String PATCH_UNKNOWN_ITEM_URI = "/item-groups/" + ITEM_GROUP_ID + "/items/" + UNKNOWN_ITEM_ID;
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
    private ObjectMapper mapper;

    @Autowired
    private ItemGroupsRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }


    @Test
    @DisplayName("Create successful itemGroup - 201 Created")
    void createItemGroupSuccessful_201Created() throws Exception {

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
    }

    @Test
    @DisplayName("Create itemGroup unsuccessful - 400 Bad Request")
    void createItemGroupUnsuccessful_400BadRequest() throws Exception {

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
    }

    @Test
    @DisplayName("create duplicate itemGroup fails - 409 Conflict")
    void createDuplicateItemGroupUnsuccessful_409Conflict() throws Exception {

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

        // Attempt to create the same item group and get failure status, 409 - CONFLICT.
        mockMvc.perform(post("/item-groups" )
                        .header(REQUEST_ID_HEADER_NAME, "12345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newItemGroupData)))
                .andExpect(status().isConflict())
                .andDo(MockMvcResultHandlers.print());
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
                .andExpect(content().string(containsString("status: must not be null")))
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
                .andExpect(
                        content().string(containsString(
                                "status: must be one of [pending, processing, satisfied, cancelled, failed]")))
                .andDo(print());
    }

    @Test
    @DisplayName("patch item patches without the digital_document_location field")
    void patchItemPatchesWithoutDocumentLocation() throws Exception {

        setUpItemGroup();

        mockMvc.perform(patch(PATCH_ITEM_URI)
                        .header(REQUEST_ID_HEADER_NAME, REQUEST_ID)
                        .contentType(APPLICATION_MERGE_PATCH)
                        .content(getJsonFromFile("patch_item_body_without_document_location")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.digital_document_location").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.status", is(EXPECTED_STATUS)))
                .andDo(print());
    }

    @Test
    @DisplayName("patch item rejects request with an invalid digital_document_location field value")
    void patchItemRejectsRequestWithInvalidDocumentLocation() throws Exception {
        mockMvc.perform(patch(PATCH_ITEM_URI)
                        .header(REQUEST_ID_HEADER_NAME, REQUEST_ID)
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
                        .contentType(APPLICATION_MERGE_PATCH)
                        .content(getJsonFromFile("patch_item_body")))
                .andExpect(status().isNotFound())
                .andExpect(content().string("")) // NOTE actual response does have meaningful content
                .andDo(print());
    }

    @Test
    @DisplayName("patch item patches without a Content-Type=application/merge-patch+json header value ")
    void patchItemRejectsRequestWithoutApplicationMergePatchContentType() throws Exception {
        mockMvc.perform(patch(PATCH_ITEM_URI)
                        .header(REQUEST_ID_HEADER_NAME, REQUEST_ID)
                        .content(getJsonFromFile("patch_item_body")))
                .andExpect(status().isUnsupportedMediaType())
                .andDo(print());
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
        newItemGroupData.setLinks(links);

        Item item = new Item();
        item.setId("CCD-768116-517930");
        item.setCompanyNumber(VALID_COMPANY_NUMBER);
        item.setCompanyName(VALID_ITEM_COMPANY_NAME);
        item.setDescriptionIdentifier(ItemDescriptionIdentifier.CERTIFIED_COPY.toString());
        item.setKind(ItemKind.ITEM_CERTIFIED_COPY.toString());
        item.setItemCosts(itemCosts);
        ItemLinks itemLinks = new ItemLinks();
        itemLinks.setOriginalItem("/orderable/certified-copies/CCD-768116-517930");
        item.setLinks(itemLinks);

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

    private void setUpItemGroup() throws IOException {
        final String itemGroup = getJsonFromFile("item_group");
        mongoTemplate.insert(Document.parse(itemGroup), "item_groups");
    }

    private String getJsonFromFile(final String fileBasename) throws IOException {
        return new String(Files.readAllBytes(Paths.get("src/test/resources/testdata/" + fileBasename + ".json")));
    }

}
