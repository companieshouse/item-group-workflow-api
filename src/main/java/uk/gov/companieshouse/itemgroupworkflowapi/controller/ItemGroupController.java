package uk.gov.companieshouse.itemgroupworkflowapi.controller;

import jakarta.json.JsonMergePatch;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.logging.Logger;

@RestController
public class ItemGroupController {

    private static final String REQUEST_ID_HEADER_NAME = "X-Request-ID";

    private final Logger logger;

    public ItemGroupController(Logger logger) {
        this.logger = logger;
    }

    // TODO DCAC-78 Use configured path
    @PatchMapping(path = "/item-groups/{itemGroupId}/items/{itemId}",
            consumes = "application/merge-patch+json")
    public ResponseEntity<Object> patchItem(
            final @RequestBody JsonMergePatch mergePatchDocument,
            final @PathVariable("itemGroupId") String itemGroupId,
            final @PathVariable("itemId") String itemId,
            final @RequestHeader(REQUEST_ID_HEADER_NAME) String requestId) {

        // TODO DCAC-78 Use structured logging
        logger.info("patchItem(" + mergePatchDocument +
                ", " + itemGroupId + ", " + itemId + ", " + requestId + ") called.");

        return ResponseEntity.ok().build();
    }

}
