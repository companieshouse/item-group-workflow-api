package uk.gov.companieshouse.itemgroupworkflowapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import uk.gov.companieshouse.itemgroupworkflowapi.config.MongoConfig;
import uk.gov.companieshouse.itemgroupworkflowapi.controller.ItemGroupController;
import uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtils;
import uk.gov.companieshouse.itemgroupworkflowapi.model.*;
import uk.gov.companieshouse.itemgroupworkflowapi.repository.ItemGroupsRepository;
import uk.gov.companieshouse.itemgroupworkflowapi.service.ItemGroupsService;
import uk.gov.companieshouse.itemgroupworkflowapi.validation.ItemGroupsValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Integration tests the {@link uk.gov.companieshouse.itemgroupworkflowapi.controller.ItemGroupController} class.
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ItemGroupController.class)
@ContextConfiguration(classes = {MongoConfig.class})
@Import({MongoConfig.class, ItemGroupController.class})
@AutoConfigureMockMvc
public class ItemGroupControllerIntegrationTest {

    private static final String EXPECTED_ORDER_NUMBER = "123456";
    private static final String VALID_DELIVERY_COMPANY_NAME = "Delivery Test Company";
    private static final String VALID_ITEM_COMPANY_NAME = "Item Test Company";
    private static final String VALID_COMPANY_NUMBER = "IG-12345-67890";
    public static final String REQUEST_ID_HEADER_NAME = "X-Request-ID";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ObjectMapper objectMapper;

    @MockBean
    private ItemGroupsRepository repository;

    @MockBean
    private ItemGroupsService service;

    @MockBean
    private LoggingUtils loggingUtils;

    @MockBean
    private ItemGroupsValidator validator;

    @MockBean
    private MappingMongoConverter mappingMongoConverter;

    @AfterEach
    void tearDown() {
        repository.findById(EXPECTED_ORDER_NUMBER).ifPresent(repository::delete);
    }

    @Test
    @DisplayName("Create successful itemGroup")
    public void createItemGroupSuccessful() throws Exception {

        // Given
        final ItemGroupData newItemGroupData = createValidNewItemGroupData();

        // When and Then
        mockMvc.perform(post("/item-groups/" )
                        .header(REQUEST_ID_HEADER_NAME, "12345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(newItemGroupData.toString()))
                .andExpect(status().isCreated())
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


}
