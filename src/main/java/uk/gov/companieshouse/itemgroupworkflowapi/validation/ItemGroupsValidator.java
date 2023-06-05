package uk.gov.companieshouse.itemgroupworkflowapi.validation;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroupData;

import java.util.ArrayList;
import java.util.List;

@Component
public class ItemGroupsValidator {
    private static final String COMPANY_NAME_KEY = "company_name_includes";
    private static final String COMPANY_NUMBER_KEY = "company_number";
    private static final String COMPANY_NAME_MISSING_MESSAGE = "missing company_name";
    private static final String COMPANY_NUMBER_MISSING_MESSAGE = "missing company_number";

    public List<String> validateCreateItemPayload(ItemGroupData dto) {
        // TODO add in validation for:
        // items[].description_identifier
        // items[].item_costs[].product_type
        // items[].kind
        //
        // The possible values are detailed in:
        // https://developer-specs.cidev.aws.chdev.org/item-group-workflow-api/reference/itemgroups/create-item-group
        //
        final List<String> errors = new ArrayList<>();

        validateDescriptionIdentifier(dto, errors);

        return errors;
    }

    private void validateDescriptionIdentifier(ItemGroupData dto, List<String> errors) {
//        errors.add("something went wrong...");
    }
}