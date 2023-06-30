package uk.gov.companieshouse.itemgroupworkflowapi.validation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.itemgroupworkflowapi.model.DeliveryDetails;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Item;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemCostProductType;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemCosts;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemDescriptionIdentifier;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroupData;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemKind;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Links;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.companieshouse.itemgroupworkflowapi.util.TestConstants.CERTIFIED_COPY_ITEM_OPTIONS;

class ItemGroupsValidatorTest {
    private static final String VALID_ORDER_NUMBER = "12345";
    private static final String VALID_DELIVERY_COMPANY_NAME = "Delivery Test Company";
    private static final String VALID_ITEM_COMPANY_NAME = "Item Test Company";
    private static final String VALID_COMPANY_NUMBER = "IG-12345-67890";
    private static final String EMPTY_ORDER_NUMBER = "";
    private static final String INVALID_DESC_ID = "XXX";
    private static final String INVALID_ITEM_KIND = "XXX";
    private static final String INVALID_ITEM_COST_PRODUCT_TYPE = "XXX";
    private static final List<Item> EMPTY_ITEMS_LIST = new ArrayList<>();
    private static final List<Item> ITEMS_LIST_SINGLE_EMPTY_ITEM = new ArrayList<>();
    private static final Links EMPTY_LINKS = new Links();

    @BeforeEach
    void beforeEach() {
        ITEMS_LIST_SINGLE_EMPTY_ITEM.add(new Item());
    }

    @AfterEach
    void afterEach() {
        ITEMS_LIST_SINGLE_EMPTY_ITEM.clear();
    }

    @Test
    @DisplayName("fair weather with all validation triggered")
    void fairWeatherAllValidationTriggeredTest() {
        final ItemGroupData dto = fairWeatherAllValidationTriggeredDto();

        ItemGroupsValidator validator = new ItemGroupsValidator();
        List<String> errors = validator.validateCreateItemData(dto);

        Assertions.assertEquals(0, errors.size());
    }
    /**
     * This DTO has all the mandatory and optional attributes present that will trigger all validation checks.
     */
    private ItemGroupData fairWeatherAllValidationTriggeredDto() {
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

        item.setItemOptions(CERTIFIED_COPY_ITEM_OPTIONS);

        List<Item> items = new ArrayList<>();
        items.add(item);
        dto.setItems(items);

        return dto;
    }

    @Test
    @DisplayName("order number missing")
    void missingOrderNumberTest() {
        final ItemGroupData dto = new ItemGroupData();
        dto.setOrderNumber(EMPTY_ORDER_NUMBER);

        ItemGroupsValidator validator = new ItemGroupsValidator();
        List<String> errors = validator.validateCreateItemData(dto);

        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals(errors.get(0), ItemGroupsValidator.ORDER_NUMBER_INVALID);
    }

    @Test
    @DisplayName("items missing")
    void missingItemsArrayTest() {
        final ItemGroupData dto = new ItemGroupData();
        dto.setOrderNumber(VALID_ORDER_NUMBER);

        ItemGroupsValidator validator = new ItemGroupsValidator();
        List<String> errors = validator.validateCreateItemData(dto);

        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals(errors.get(0), ItemGroupsValidator.ITEM_GROUP_ITEMS_MISSING);
    }

    @Test
    @DisplayName("items empty")
    void emptyItemsArrayTest() {
        final ItemGroupData dto = new ItemGroupData();
        dto.setOrderNumber(VALID_ORDER_NUMBER);
        dto.setItems(EMPTY_ITEMS_LIST);

        ItemGroupsValidator validator = new ItemGroupsValidator();
        List<String> errors = validator.validateCreateItemData(dto);

        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals(errors.get(0), ItemGroupsValidator.ITEM_GROUP_ITEMS_MISSING);
    }

    @Test
    @DisplayName("links missing")
    void missingLinksTest() {
        final ItemGroupData dto = new ItemGroupData();
        dto.setOrderNumber(VALID_ORDER_NUMBER);
        dto.setItems(ITEMS_LIST_SINGLE_EMPTY_ITEM);

        ItemGroupsValidator validator = new ItemGroupsValidator();
        List<String> errors = validator.validateCreateItemData(dto);

        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals(errors.get(0), ItemGroupsValidator.ITEM_GROUP_LINKS_MISSING);
    }

    @Test
    @DisplayName("links present - missing order number")
    void linksPresentMissingOrderNumberTest() {
        final ItemGroupData dto = new ItemGroupData();
        dto.setOrderNumber(VALID_ORDER_NUMBER);
        dto.setItems(ITEMS_LIST_SINGLE_EMPTY_ITEM);
        dto.setLinks(EMPTY_LINKS);

        ItemGroupsValidator validator = new ItemGroupsValidator();
        List<String> errors = validator.validateCreateItemData(dto);

        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals(errors.get(0), ItemGroupsValidator.LINKS_ORDER_NUMBER_MISSING);
    }

    @Test
    @DisplayName("invalid Item Description Identifier")
    void invalidItemDescriptionIdentifierTest() {
        final ItemGroupData dto = invalidItemDescriptionIdentifierDto();

        ItemGroupsValidator validator = new ItemGroupsValidator();
        List<String> errors = validator.validateCreateItemData(dto);

        Assertions.assertEquals(1, errors.size());
        Assertions.assertTrue(errors.get(0).contains(ItemGroupsValidator.INVALID_DESCRIPTION_IDENTIFIER));
        Assertions.assertTrue(errors.get(0).contains(INVALID_DESC_ID));
    }
    /**
     * DTO for invalid ItemDescriptionIdentifier
     */
    private ItemGroupData invalidItemDescriptionIdentifierDto() {
        final ItemGroupData dto = new ItemGroupData();
        dto.setOrderNumber(VALID_ORDER_NUMBER);

        Links links = new Links();
        links.setOrder(VALID_ORDER_NUMBER);
        dto.setLinks(links);

        Item item = new Item();
        item.setDescriptionIdentifier(INVALID_DESC_ID); // Invalid
        item.setKind(ItemKind.ITEM_CERTIFIED_COPY.toString());

        item.setItemOptions(CERTIFIED_COPY_ITEM_OPTIONS);

        List<Item> items = new ArrayList<>();
        items.add(item);
        dto.setItems(items);

        return dto;
    }

    @Test
    @DisplayName("invalid Item Costs Product Type")
    void invalidItemCostsProductTypeTest() {
        final ItemGroupData dto = invalidItemCostsProductTypeDto();

        ItemGroupsValidator validator = new ItemGroupsValidator();
        List<String> errors = validator.validateCreateItemData(dto);

        Assertions.assertEquals(1, errors.size());
        Assertions.assertTrue(errors.get(0).contains(ItemGroupsValidator.INVALID_ITEM_COST_PRODUCT_TYPE));
        Assertions.assertTrue(errors.get(0).contains(INVALID_ITEM_COST_PRODUCT_TYPE));
    }
    /**
     * DTO for invalid ItemCostProductType
     */
    private ItemGroupData invalidItemCostsProductTypeDto() {
        final ItemGroupData dto = new ItemGroupData();
        dto.setOrderNumber(VALID_ORDER_NUMBER);

        Links links = new Links();
        links.setOrder(VALID_ORDER_NUMBER);
        dto.setLinks(links);

        Item item = new Item();
        item.setDescriptionIdentifier(ItemDescriptionIdentifier.CERTIFIED_COPY.toString());
        item.setKind(ItemKind.ITEM_CERTIFIED_COPY.toString());

        ItemCosts itemCost = new ItemCosts();
        itemCost.setProductType(INVALID_ITEM_COST_PRODUCT_TYPE); // Invalid

        List<ItemCosts> itemCosts = new ArrayList<>();
        itemCosts.add(itemCost);
        item.setItemCosts(itemCosts);

        item.setItemOptions(CERTIFIED_COPY_ITEM_OPTIONS);

        List<Item> items = new ArrayList<>();
        items.add(item);
        dto.setItems(items);

        return dto;
    }

    @Test
    @DisplayName("invalid Item Kind")
    void invalidItemKindTest() {
        final ItemGroupData dto = invalidItemKindDto();

        ItemGroupsValidator validator = new ItemGroupsValidator();
        List<String> errors = validator.validateCreateItemData(dto);

        Assertions.assertEquals(1, errors.size());
        Assertions.assertTrue(errors.get(0).contains(ItemGroupsValidator.INVALID_ITEM_KIND_NAME));
        Assertions.assertTrue(errors.get(0).contains(INVALID_ITEM_KIND));
    }
    /**
     * DTO for invalid ItemKind
     */
    private ItemGroupData invalidItemKindDto() {
        final ItemGroupData dto = new ItemGroupData();
        dto.setOrderNumber(VALID_ORDER_NUMBER);

        Links links = new Links();
        links.setOrder(VALID_ORDER_NUMBER);
        dto.setLinks(links);

        Item item = new Item();
        item.setDescriptionIdentifier(ItemDescriptionIdentifier.CERTIFIED_COPY.toString());
        item.setKind(INVALID_ITEM_KIND);    // Invalid

        List<Item> items = new ArrayList<>();
        items.add(item);
        dto.setItems(items);

        return dto;
    }

    @Test
    @DisplayName("delivery details - missing item company NAME")
    void deliveryDetailsCompanyNamePresentAndCompanyNameMissingTest() {
        final ItemGroupData dto = deliveryDetailsCompanyNamePresentAndCompanyNameMissingDto();

        ItemGroupsValidator validator = new ItemGroupsValidator();
        List<String> errors = validator.validateCreateItemData(dto);

        Assertions.assertEquals(1, errors.size());
        Assertions.assertTrue(errors.get(0).contains(ItemGroupsValidator.COMPANY_NAME_MISSING));
    }
    /**
     * DTO for delivery details company name present - missing item company NAME
     */
    private ItemGroupData deliveryDetailsCompanyNamePresentAndCompanyNameMissingDto() {
        final ItemGroupData dto = new ItemGroupData();
        dto.setOrderNumber(VALID_ORDER_NUMBER);

        DeliveryDetails deliveryDetails = new DeliveryDetails();
        deliveryDetails.setCompanyName(VALID_DELIVERY_COMPANY_NAME);
        dto.setDeliveryDetails(deliveryDetails);

        Links links = new Links();
        links.setOrder(VALID_ORDER_NUMBER);
        dto.setLinks(links);

        Item item = new Item();
        item.setCompanyNumber(VALID_COMPANY_NUMBER);    // company NAME missing
        item.setDescriptionIdentifier(ItemDescriptionIdentifier.CERTIFIED_COPY.toString());
        item.setKind(ItemKind.ITEM_CERTIFIED_COPY.toString());

        item.setItemOptions(CERTIFIED_COPY_ITEM_OPTIONS);

        List<Item> items = new ArrayList<>();
        items.add(item);
        dto.setItems(items);

        return dto;
    }

    @Test
    @DisplayName("delivery details - missing item company NUMBER")
    void deliveryDetailsCompanyNamePresentAndCompanyNumberMissingTest() {
        final ItemGroupData dto = deliveryDetailsCompanyNamePresentAndCompanyNumberMissingDto();

        ItemGroupsValidator validator = new ItemGroupsValidator();
        List<String> errors = validator.validateCreateItemData(dto);

        Assertions.assertEquals(1, errors.size());
        Assertions.assertTrue(errors.get(0).contains(ItemGroupsValidator.COMPANY_NUMBER_MISSING));
    }
    /**
     * DTO for delivery details company name present - missing item company NUMBER
     */
    private ItemGroupData deliveryDetailsCompanyNamePresentAndCompanyNumberMissingDto() {
        final ItemGroupData dto = new ItemGroupData();
        dto.setOrderNumber(VALID_ORDER_NUMBER);

        DeliveryDetails deliveryDetails = new DeliveryDetails();
        deliveryDetails.setCompanyName(VALID_DELIVERY_COMPANY_NAME);
        dto.setDeliveryDetails(deliveryDetails);

        Links links = new Links();
        links.setOrder(VALID_ORDER_NUMBER);
        dto.setLinks(links);

        Item item = new Item();
        item.setCompanyName(VALID_ITEM_COMPANY_NAME);    // company NUMBER missing
        item.setDescriptionIdentifier(ItemDescriptionIdentifier.CERTIFIED_COPY.toString());
        item.setKind(ItemKind.ITEM_CERTIFIED_COPY.toString());

        item.setItemOptions(CERTIFIED_COPY_ITEM_OPTIONS);

        List<Item> items = new ArrayList<>();
        items.add(item);
        dto.setItems(items);

        return dto;
    }
}