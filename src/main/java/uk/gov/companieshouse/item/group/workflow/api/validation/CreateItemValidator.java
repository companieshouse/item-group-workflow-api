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
//
//        validateCompanyNumberName(dto, errors);
//
//        return errors;
    }

    private void validateCompanyNumberName(ItemGroupJsonPayload dto, List<String> errors){
        validateCompanyNumber(dto, errors);
        validateCompanyName(dto, errors);
    }

    private void validateCompanyNumber(ItemGroupJsonPayload dto, List<String> errors) {
        if(StringUtils.isBlank(dto.getCompanyNumber())) {
            DataMap dataMap = new DataMap.Builder()
                .companyNumber(COMPANY_NUMBER_MISSING_MESSAGE)
                .build();
            errors.add((String) dataMap.getLogMap().get(COMPANY_NUMBER_KEY));
        }
    }

    private void validateCompanyName(ItemGroupJsonPayload dto, List<String> errors) {
        if(StringUtils.isBlank(dto.getCompanyName())) {
            DataMap dataMap = new DataMap.Builder()
                .companyName(COMPANY_NAME_MISSING_MESSAGE)
                .build();
            errors.add((String) dataMap.getLogMap().get(COMPANY_NAME_KEY));
        }
    }
}