package uk.gov.companieshouse.itemgroupworkflowapi.validation;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroupData;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Links;
import uk.gov.companieshouse.itemgroupworkflowapi.service.ItemGroupsService;

import java.util.ArrayList;
import java.util.List;

@Component
public class ItemGroupsValidator {
    private static final String COMPANY_NAME_KEY = "company_name_includes";
    private static final String COMPANY_NUMBER_KEY = "company_number";
    private static final String COMPANY_NAME_MISSING_MESSAGE = "missing company_name";
    private static final String COMPANY_NUMBER_INVALID = "order number invalid";

    private final ItemGroupsService itemGroupsService;

    public ItemGroupsValidator(ItemGroupsService itemGroupsService) {
        this.itemGroupsService = itemGroupsService;
    }

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
        validateDescriptionIdentifier(dto, errors);
        validateItemCostsProductType(dto, errors);
        validateItemKind(dto, errors);

        return errors;
    }

    private boolean isOrderNumberValid(ItemGroupData dto, List<String> errors) {
        boolean result = true;
        String orderNumber = dto.getOrderNumber();

        if (orderNumber == null || orderNumber.isEmpty() || orderNumber.isBlank()) {
            errors.add("order number invalid");
            result = false;
        }

        return result;
    }

    private void validateCompanyNumber(ItemGroupData dto, List<String> errors) {
    }

    private void validateItems(ItemGroupData dto, List<String> errors) {
        var items = dto.getItems();

        if (items == null || items.isEmpty()) {
            errors.add("items missing");
        }
    }

    private void validateLinks(ItemGroupData dto, List<String> errors) {
        Links links = dto.getLinks();

        if (links == null) {
            errors.add("links missing");
            return;
        }

        String order = links.getOrder();

        if (order == null || order.isBlank() || order.isEmpty()) {
            errors.add("links missing order number");
        }
    }

    private void validateDescriptionIdentifier(ItemGroupData dto, List<String> errors) {
        // TODO - DCAC-47 - for future validation.
    }

    private void validateItemCostsProductType(ItemGroupData dto, List<String> errors) {
        // TODO - DCAC-47 - for future validation.
    }

    private void validateItemKind(ItemGroupData dto, List<String> errors) {
        // TODO - DCAC-47 - for future validation.
    }
}