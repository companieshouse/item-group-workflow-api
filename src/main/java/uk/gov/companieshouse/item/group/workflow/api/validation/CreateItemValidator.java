package uk.gov.companieshouse.item.group.workflow.api.validation;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.item.group.workflow.api.model.ItemGroupJsonPayload;
import uk.gov.companieshouse.logging.util.DataMap;

import java.util.ArrayList;
import java.util.List;

@Component
public class CreateItemValidator {
    private static final String COMPANY_NAME_KEY = "company_name_includes";
    private static final String COMPANY_NUMBER_KEY = "company_number";
    private static final String COMPANY_NAME_MISSING_MESSAGE = "missing company_name";
    private static final String COMPANY_NUMBER_MISSING_MESSAGE = "missing company_number";
    public List<String> validateCreateItemPayload(ItemGroupJsonPayload dto) {
        final List<String> errors = new ArrayList<>();
        return errors;
    }
}