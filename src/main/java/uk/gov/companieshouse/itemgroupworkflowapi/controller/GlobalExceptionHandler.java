package uk.gov.companieshouse.itemgroupworkflowapi.controller;

import static uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtilsConfiguration.APPLICATION_NAMESPACE;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Returns HTTP Status 500 when there is an issue propagating the item status update.
     *
     * @param rce exception
     * @return response
     */
    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<Object> handleItemStatusUpdatePropagationFailure(
        final RestClientException rce) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Error in " + APPLICATION_NAMESPACE + ": " + rce.getMessage());
    }

}
