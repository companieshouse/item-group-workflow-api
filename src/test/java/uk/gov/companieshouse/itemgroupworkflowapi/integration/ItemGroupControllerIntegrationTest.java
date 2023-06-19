package uk.gov.companieshouse.itemgroupworkflowapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import uk.gov.companieshouse.itemgroupworkflowapi.model.DeliveryDetails;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Item;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemCostProductType;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemCosts;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemDescriptionIdentifier;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroupData;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemKind;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Links;
import uk.gov.companieshouse.itemgroupworkflowapi.repository.ItemGroupsRepository;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests the {@link uk.gov.companieshouse.itemgroupworkflowapi.controller.ItemGroupController} class.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ComponentScan("uk.gov.companieshouse.itemgroupworkflowapi")
public class ItemGroupControllerIntegrationTest {

    private static final String EXPECTED_ORDER_NUMBER = "123456";
    private static final String VALID_DELIVERY_COMPANY_NAME = "Delivery Test Company";
    private static final String VALID_ITEM_COMPANY_NAME = "Item Test Company";
    private static final String VALID_COMPANY_NUMBER = "IG-12345-67890";
    public static final String REQUEST_ID_HEADER_NAME = "X-Request-ID";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ItemGroupsRepository repository;

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
        item.setCompanyNumber(VALID_COMPANY_NUMBER);
        item.setCompanyName(VALID_ITEM_COMPANY_NAME);
        item.setDescriptionIdentifier(ItemDescriptionIdentifier.CERTIFIED_COPY.toString());
        item.setKind(ItemKind.ITEM_CERTIFIED_COPY.toString());
        item.setItemCosts(itemCosts);

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

}
