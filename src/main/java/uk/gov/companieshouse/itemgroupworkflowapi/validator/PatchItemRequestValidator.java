package uk.gov.companieshouse.itemgroupworkflowapi.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.itemgroupworkflowapi.util.ApiErrorBuilder;
import uk.gov.companieshouse.itemgroupworkflowapi.dto.ItemPatchValidationDto;
import uk.gov.companieshouse.itemgroupworkflowapi.util.FieldNameConverter;

import jakarta.json.JsonMergePatch;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static uk.gov.companieshouse.itemgroupworkflowapi.controller.ApiErrors.ERROR_TYPE_VALIDATION;
import static uk.gov.companieshouse.itemgroupworkflowapi.controller.ApiErrors.ERR_JSON_PROCESSING;
import static uk.gov.companieshouse.itemgroupworkflowapi.controller.ApiErrors.OBJECT_LOCATION_TYPE;

/**
 * Implements validation of the request payload specific to the patch item request only.
 */
@Component
public class PatchItemRequestValidator {

    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final FieldNameConverter converter;

    /**
     * Constructor.
     * @param objectMapper the object mapper this relies upon to deserialise JSON
     * @param validator the validator this relies upon to validate DTOs
     * @param converter the converter this uses to present field names as they appear in the request JSON payload
     */
    public PatchItemRequestValidator(final ObjectMapper objectMapper,
                                     final Validator validator,
                                     final FieldNameConverter converter) {
        this.objectMapper = objectMapper;
        this.validator = validator;
        this.converter = converter;
    }

    /**
     * Validates the patch provided, returning any errors found.
     * @param patch the item to be validated
     * @return the errors found, which will be empty if the item is found to be valid
     */
    public List<ApiError> getValidationErrors(final JsonMergePatch patch) {
        try {
            final ItemPatchValidationDto dto =
                    objectMapper.readValue(patch.toJsonValue().toString(), ItemPatchValidationDto.class);
            final Set<ConstraintViolation<ItemPatchValidationDto>> violations = validator.validate(dto);
            return violations.stream()
                    .sorted(Comparator.comparing(a -> a.getPropertyPath().toString()))
                    .map(this::raiseError)
                    .collect(Collectors.toList());
        } catch (JsonProcessingException jpe) {
            return singletonList(ERR_JSON_PROCESSING);
        }
    }

    private ApiError raiseError(ConstraintViolation<ItemPatchValidationDto> violation) {
        String fieldName = violation.getPropertyPath().toString();
        String snakeCaseFieldName = converter.fromUpperCamelToSnakeCase(fieldName);
        return ApiErrorBuilder.builder(
                new ApiError(converter.fromLowerUnderscoreToLowerHyphenCase(snakeCaseFieldName) + "-error",
                        snakeCaseFieldName, OBJECT_LOCATION_TYPE, ERROR_TYPE_VALIDATION))
                .withErrorMessage(snakeCaseFieldName + ": " + violation.getMessage())
                .build();
    }
}
