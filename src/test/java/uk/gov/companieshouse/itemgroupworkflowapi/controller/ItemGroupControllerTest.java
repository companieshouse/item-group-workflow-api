package uk.gov.companieshouse.itemgroupworkflowapi.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtils;
import uk.gov.companieshouse.itemgroupworkflowapi.model.DeliveryDetails;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Item;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemCostProductType;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemCosts;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemDescriptionIdentifier;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroup;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroupData;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemKind;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Links;
import uk.gov.companieshouse.itemgroupworkflowapi.repository.ItemGroupsRepository;
import uk.gov.companieshouse.itemgroupworkflowapi.service.ItemGroupsService;
import uk.gov.companieshouse.itemgroupworkflowapi.validation.ItemGroupsValidator;
import uk.gov.companieshouse.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ItemGroupControllerTest {
    private static final String VALID_ORDER_NUMBER = "lucky string";
    private static final String VALID_DELIVERY_COMPANY_NAME = "Delivery Test Company";
    private static final String VALID_ITEM_COMPANY_NAME = "Item Test Company";
    private static final String VALID_COMPANY_NUMBER = "IG-12345-67890";

    @InjectMocks
    ItemGroupController controller;

    @Mock
    private LoggingUtils loggingUtils;

    @Mock
    private Logger logger;

    @Mock
    private HttpServletRequest request;

    @Mock
    private ItemGroupsValidator requestValidator;

    @Mock
    private ItemGroupsService itemGroupsService;

    private ItemGroupsRepository itemGroupsRepository;

    @Test
    @DisplayName("empty test")
    void emptyTest() {
    }

    @Test
    @DisplayName("create item group return 201 created")
    void createItemGroupReturnCreated201()  throws Exception {
        final String requestId = "12345";
        final ItemGroupData itemGroupData = fairWeatherItemGroupsDto();
        ItemGroup itemGroup = new ItemGroup();
        itemGroup.setData(itemGroupData);

        when(itemGroupsService.createItemGroup(itemGroupData)).thenReturn(itemGroup);
        when(loggingUtils.getLogger()).thenReturn(logger);

        final ResponseEntity<Object> response = controller.createItemGroup(requestId, itemGroupData);
        //
        // Verify HttpStatus.CREATED returned.
        //
        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
        //
        // ItemGroup
        //
        final ItemGroup createdItemGroup = (ItemGroup) response.getBody();
        assert(createdItemGroup != null);
        assertThat(createdItemGroup.getData().getOrderNumber(), is(VALID_ORDER_NUMBER));

        assertThat(createdItemGroup.getData().getOrderNumber(), is(VALID_ORDER_NUMBER));
        assertThat(createdItemGroup.getData().getDeliveryDetails().getCompanyName(), is(VALID_DELIVERY_COMPANY_NAME));
        assertThat(createdItemGroup.getData().getLinks().getOrder(), is(VALID_ORDER_NUMBER));
        //
        // Item
        //
        assertEquals(1, createdItemGroup.getData().getItems().size());
        Item item = createdItemGroup.getData().getItems().get(0);
        assertNotNull(item);

        assertThat(item.getCompanyNumber(), is(VALID_COMPANY_NUMBER));
        assertThat(item.getCompanyName(), is(VALID_ITEM_COMPANY_NAME));
        assertThat(item.getDescriptionIdentifier(), is(ItemDescriptionIdentifier.CERTIFIED_COPY.toString()));
        assertThat(item.getKind(), is(ItemKind.ITEM_CERTIFIED_COPY.toString()));
        //
        // ItemCosts
        //
        assertEquals(1, item.getItemCosts().size());
        ItemCosts createdItemCosts = item.getItemCosts().get(0);
        assertNotNull(createdItemCosts);

        assertThat(createdItemCosts.getProductType(), is(ItemCostProductType.CERTIFIED_COPY_INCORPORATION.toString()));
    }

    @Test
    @DisplayName("create item group return 400 invalid request")
    void createItemGroupReturnCreated400()  throws Exception {
        final String requestId = "12345";
        final ItemGroupData itemGroupData = fairWeatherItemGroupsDto();
        ItemGroup itemGroup = new ItemGroup();
        itemGroup.setData(itemGroupData);

        when(itemGroupsService.createItemGroup(itemGroupData)).thenReturn(itemGroup);
        when(loggingUtils.getLogger()).thenReturn(logger);

        final ResponseEntity<Object> response = controller.createItemGroup(requestId, itemGroupData);
        //
        // Verify HttpStatus.BAD_REQUEST returned.
        //
        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        //
        // ItemGroup
        //
        final ItemGroup createdItemGroup = (ItemGroup) response.getBody();
        assert(createdItemGroup != null);
        assertThat(createdItemGroup.getData().getOrderNumber(), is(VALID_ORDER_NUMBER));

        assertThat(createdItemGroup.getData().getOrderNumber(), is(VALID_ORDER_NUMBER));
        assertThat(createdItemGroup.getData().getDeliveryDetails().getCompanyName(), is(VALID_DELIVERY_COMPANY_NAME));
        assertThat(createdItemGroup.getData().getLinks().getOrder(), is(VALID_ORDER_NUMBER));
        //
        // Item
        //
        assertEquals(1, createdItemGroup.getData().getItems().size());
        Item item = createdItemGroup.getData().getItems().get(0);
        assertNotNull(item);

        assertThat(item.getCompanyNumber(), is(VALID_COMPANY_NUMBER));
        assertThat(item.getCompanyName(), is(VALID_ITEM_COMPANY_NAME));
        assertThat(item.getDescriptionIdentifier(), is(ItemDescriptionIdentifier.CERTIFIED_COPY.toString()));
        assertThat(item.getKind(), is(ItemKind.ITEM_CERTIFIED_COPY.toString()));
        //
        // ItemCosts
        //
        assertEquals(1, item.getItemCosts().size());
        ItemCosts createdItemCosts = item.getItemCosts().get(0);
        assertNotNull(createdItemCosts);

        assertThat(createdItemCosts.getProductType(), is(ItemCostProductType.CERTIFIED_COPY_INCORPORATION.toString()));
    }
    /**
     * Fair weather with all validation triggered DTO
     */
    private ItemGroupData fairWeatherItemGroupsDto() {
        final ItemGroupData dto = new ItemGroupData();
//        dto.setOrderNumber(VALID_ORDER_NUMBER);
//
//        DeliveryDetails deliveryDetails = new DeliveryDetails();
//        deliveryDetails.setCompanyName(VALID_DELIVERY_COMPANY_NAME);
//        dto.setDeliveryDetails(deliveryDetails);
//
//        ItemCosts itemCost = new ItemCosts();
//        itemCost.setProductType(ItemCostProductType.CERTIFIED_COPY_INCORPORATION.toString());
//
//        List<ItemCosts> itemCosts = new ArrayList<>();
//        itemCosts.add(itemCost);
//
//        Links links = new Links();
//        links.setOrder(VALID_ORDER_NUMBER);
//        dto.setLinks(links);
//
//        Item item = new Item();
//        item.setCompanyNumber(VALID_COMPANY_NUMBER);
//        item.setCompanyName(VALID_ITEM_COMPANY_NAME);
//        item.setDescriptionIdentifier(ItemDescriptionIdentifier.CERTIFIED_COPY.toString());
//        item.setKind(ItemKind.ITEM_CERTIFIED_COPY.toString());
//        item.setItemCosts(itemCosts);
//
//        List<Item> items = new ArrayList<>();
//        items.add(item);
//        dto.setItems(items);

        return dto;
    }
    /**
     * Invalidate DTO
     */
    private ItemGroupData invalidItemGroupsDto() {
        final ItemGroupData dto = new ItemGroupData();
        dto.setOrderNumber(VALID_ORDER_NUMBER);

        DeliveryDetails deliveryDetails = new DeliveryDetails();
        deliveryDetails.setCompanyName(VALID_DELIVERY_COMPANY_NAME);
        dto.setDeliveryDetails(deliveryDetails);

        ItemCosts itemCost = new ItemCosts();
        itemCost.setProductType(ItemCostProductType.CERTIFIED_COPY_INCORPORATION.toString());

        List<ItemCosts> itemCosts = new ArrayList<>();
        itemCosts.add(itemCost);

        Links links = new Links();
        links.setOrder(VALID_ORDER_NUMBER);
        dto.setLinks(links);

        Item item = new Item();
        item.setCompanyNumber(VALID_COMPANY_NUMBER);
        item.setCompanyName(VALID_ITEM_COMPANY_NAME);
        item.setDescriptionIdentifier(ItemDescriptionIdentifier.CERTIFIED_COPY.toString());
        item.setKind(ItemKind.ITEM_CERTIFIED_COPY.toString());
        item.setItemCosts(itemCosts);

        List<Item> items = new ArrayList<>();
        items.add(item);
        dto.setItems(items);

        return dto;
    }
}