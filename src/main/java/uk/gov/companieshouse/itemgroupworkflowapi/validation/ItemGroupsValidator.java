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
    public List<String> validateCreateItemPayload(ItemGroupData dto) {
        // TODO - DCAC-47 - add in validation for:
        // items[].description_identifier
        // items[].item_costs[].product_type
        // items[].kind
        //
        // The possible values are detailed in:
        // https://developer-specs.cidev.aws.chdev.org/item-group-workflow-api/reference/itemgroups/create-item-group
        //
        final List<String> errors = new ArrayList<>();

        if(!isOrderNumberValid(dto, errors))
            return errors;

        validateCompanyNumber(dto, errors);
        validateItems(dto, errors);
        validateLinks(dto, errors);
        validateItemDescriptionIdentifier(dto, errors);
        validateItemCostsProductType(dto, errors);
        validateItemKind(dto, errors);

        return errors;
    }

    private boolean isOrderNumberValid(ItemGroupData dto, List<String> errors) {
        boolean result = true;
        String orderNumber = dto.getOrderNumber();

        if (isNull(orderNumber) || orderNumber.isEmpty() || orderNumber.isBlank()) {
            errors.add("order number invalid");
            result = false;
        }

        return result;
    }

    private void validateCompanyNumber(ItemGroupData dto, List<String> errors) {
    }

    private void validateItems(ItemGroupData dto, List<String> errors) {
        var items = dto.getItems();

        if (isNull(items) || items.isEmpty()) {
            errors.add("items missing");
        }
    }

    private void validateLinks(ItemGroupData dto, List<String> errors) {
        Links links = dto.getLinks();

        if (isNull(links)) {
            errors.add("links missing");
            return;
        }

        String order = links.getOrder();

        if (isNull(order) || order.isBlank() || order.isEmpty()) {
            errors.add("links missing order number");
        }
    }

    private void validateItemDescriptionIdentifier(ItemGroupData dto, List<String> errors) {
        // TODO - DCAC-47 - for future validation.

        for (Item item : dto.getItems()) {
            String descriptionIdentifier = item.getDescriptionIdentifier();

            if (isNull(ItemDescriptionIdentifier.getEnumValue(descriptionIdentifier))) {
                errors.add("invalid item description identifier " + descriptionIdentifier);
            }
        }
    }

    private void validateItemCostsProductType(ItemGroupData dto, List<String> errors) {
        // TODO - DCAC-47 - for future validation.

        for (Item item : dto.getItems()) {
            for (ItemCosts itemCost : item.getItemCosts()) {
                String productType = itemCost.getItemCost();

                if (isNull(ItemCostProductType.getEnumValue(productType))) {
                    errors.add("invalid item cost product type " + productType);
                }
            }
        }
    }

    private void validateItemKind(ItemGroupData dto, List<String> errors) {
        // TODO - DCAC-47 - for future validation.

        for (Item item : dto.getItems()) {
            String itemKind = item.getKind();

            if (isNull(ItemKind.getEnumValue(itemKind))) {
                errors.add("invalid item kind " + itemKind);
            }
        }
    }
}