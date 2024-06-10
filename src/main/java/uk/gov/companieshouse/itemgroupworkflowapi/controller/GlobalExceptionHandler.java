package uk.gov.companieshouse.itemgroupworkflowapi.controller;

import static uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtilsConfiguration.APPLICATION_NAME_SPACE;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.companieshouse.itemgroupworkflowapi.exception.ItemStatusUpdatePropagationException;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Returns HTTP Status 500 when there is an issue propagating the item status update.
     *
     * @param isupe exception thrown when there is an issue propagating the item status update
     * @return response with payload reporting underlying cause
     */
    @ExceptionHandler(ItemStatusUpdatePropagationException.class)
    public ResponseEntity<Object> handleItemStatusUpdatePropagationFailure(
        final ItemStatusUpdatePropagationException isupe) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Error in " + APPLICATION_NAME_SPACE + ": " + isupe.getMessage());
    }

}
