package uk.gov.companieshouse.itemgroupworkflowapi.validation;

import static java.util.Objects.isNull;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Item;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemCosts;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroupData;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Links;

import java.util.ArrayList;
import java.util.List;

@Component
public class ItemGroupsValidator {
    public static final String ORDER_NUMBER_INVALID = "Items Group : order number invalid";
    public static final String ITEM_GROUP_ITEMS_MISSING = "Items Group : items not provided";
    public static final String LINKS_MISSING = "Items Group : links not provided";
    public static final String LINKS_ORDER_NUMBER_MISSING = "Items Group : link order number not provided";
    public static final String INVALID_DESCRIPTION_ID = "Items Group : invalid item description identifier : ";
    public static final String INVALID_ITEM_COST_PRODUCT_TYPE = "Items Group : invalid item cost product type : ";
    public static final String INVALID_ITEM_KIND = "Items Group : invalid item kind : ";

    public List<String> validateCreateItemPayload(ItemGroupData itemGroupData) {
        final List<String> errors = new ArrayList<>();

        if(!isOrderNumberValid(itemGroupData, errors))
            return errors;

        validateCompanyNumber(itemGroupData, errors);
        validateItems(itemGroupData, errors);
        validateLinks(itemGroupData, errors);
        validateItemDescriptionIdentifier(itemGroupData, errors);
        validateItemCostsProductType(itemGroupData, errors);
        validateItemKind(itemGroupData, errors);

        return errors;
    }

    private boolean isOrderNumberValid(ItemGroupData itemGroupData, List<String> errors) {
        boolean result = true;
        String orderNumber = itemGroupData.getOrderNumber();

        if (isNull(orderNumber) || orderNumber.isEmpty() || orderNumber.isBlank()) {
            errors.add(ORDER_NUMBER_INVALID);
            result = false;
        }

        return result;
    }

    private void validateCompanyNumber(ItemGroupData itemGroupData, List<String> errors) {
    }

    private void validateItems(ItemGroupData itemGroupData, List<String> errors) {
        List<Item> items = itemGroupData.getItems();

        if (isNull(items) || items.isEmpty()) {
            errors.add(ITEM_GROUP_ITEMS_MISSING);
        }
    }

    private void validateLinks(ItemGroupData itemGroupData, List<String> errors) {
        Links links = itemGroupData.getLinks();

        if (isNull(links)) {
            errors.add(LINKS_MISSING);
            return;
        }

        String order = links.getOrder();

        if (isNull(order) || order.isBlank() || order.isEmpty()) {
            errors.add(LINKS_ORDER_NUMBER_MISSING);
        }
    }

    private void validateItemDescriptionIdentifier(ItemGroupData itemGroupData, List<String> errors) {
        for (Item item : itemGroupData.getItems()) {
            String descriptionIdentifier = item.getDescriptionIdentifier();

            if (isNull(ItemDescriptionIdentifier.getEnumValue(descriptionIdentifier))) {
                errors.add(INVALID_DESCRIPTION_ID + descriptionIdentifier);
            }
        }
    }

    private void validateItemCostsProductType(ItemGroupData itemGroupData, List<String> errors) {
        for (Item item : itemGroupData.getItems()) {
            for (ItemCosts itemCost : item.getItemCosts()) {
                String productType = itemCost.getItemCost();

                if (isNull(ItemCostProductType.getEnumValue(productType))) {
                    errors.add(INVALID_ITEM_COST_PRODUCT_TYPE + productType);
                }
            }
        }
    }

    private void validateItemKind(ItemGroupData itemGroupData, List<String> errors) {
        for (Item item : itemGroupData.getItems()) {
            String itemKind = item.getKind();

            if (isNull(ItemKind.getEnumValue(itemKind))) {
                errors.add(INVALID_ITEM_KIND + itemKind);
            }
        }
    }
}