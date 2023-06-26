package uk.gov.companieshouse.itemgroupworkflowapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.api.model.ApiResponse;

import java.util.List;

public final class ApiErrors {

    private static final String JSON_PROCESSING_ERROR = "json-processing-error";
    private static final String JSON_PROCESSING_LOCATION = "item";
    public static final String OBJECT_LOCATION_TYPE = "object";

    public static final String ERROR_TYPE_VALIDATION = "ch:validation";
    private static final String ERROR_TYPE_SERVICE = "ch:service";

    public static final ApiError ERR_JSON_PROCESSING = new ApiError(JSON_PROCESSING_ERROR, JSON_PROCESSING_LOCATION, OBJECT_LOCATION_TYPE, ERROR_TYPE_SERVICE);

    private ApiErrors() {}

    public static ResponseEntity<Object> errorResponse(HttpStatus httpStatus, List<ApiError> errors) {
        return ResponseEntity.status(httpStatus).body(new ApiResponse<>(errors));
    }
}
