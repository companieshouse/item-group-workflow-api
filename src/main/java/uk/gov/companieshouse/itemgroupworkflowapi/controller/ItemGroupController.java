package uk.gov.companieshouse.itemgroupworkflowapi.controller;

import jakarta.json.JsonMergePatch;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.logging.Logger;

import static uk.gov.companieshouse.itemgroupworkflowapi.util.Constants.REQUEST_ID_HEADER_NAME;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.PatchMediaType.APPLICATION_MERGE_PATCH_VALUE;

@RestController
public class ItemGroupController {

    private static final String PATCH_ITEM_URI =
            "${uk.gov.companieshouse.itemgroupworkflowapi.patchitem}";

    private final Logger logger;

    public ItemGroupController(Logger logger) {
        this.logger = logger;
    }

    @PatchMapping(path = PATCH_ITEM_URI, consumes = APPLICATION_MERGE_PATCH_VALUE)
    public ResponseEntity<Object> patchItem(
            final @RequestBody JsonMergePatch mergePatchDocument,
            final @PathVariable("itemGroupId") String itemGroupId,
            final @PathVariable("itemId") String itemId,
            final @RequestHeader(REQUEST_ID_HEADER_NAME) String requestId) {

        // TODO DCAC-78 Use structured logging
        logger.info("patchItem(" + mergePatchDocument +
                ", " + itemGroupId + ", " + itemId + ", " + requestId + ") called.");

        // TODO DCAC-78 Validate request

        // TODO DCAC-78 Retrieve item group, item

        // TODO DCAC-78 Merge patch into retrieved item

        // TODO DCAC-78 Post-merge validation - is any required?

        // TODO DCAC-78 Save patched item

        // TODO DCAC-78 Build response DTO and return it as body

        return ResponseEntity.ok().build();
    }

}
