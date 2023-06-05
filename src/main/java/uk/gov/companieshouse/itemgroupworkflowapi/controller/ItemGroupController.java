package uk.gov.companieshouse.itemgroupworkflowapi.controller;

import jakarta.json.JsonMergePatch;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtils;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroupCreate;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroupJsonPayload;
import uk.gov.companieshouse.itemgroupworkflowapi.service.ItemGroupsService;
import uk.gov.companieshouse.itemgroupworkflowapi.validator.CreateItemValidator;
import uk.gov.companieshouse.itemgroupworkflowapi.validator.PatchItemRequestValidator;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtils.CREATE_ITEM_GROUP_ERROR_PREFIX;
import static uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtils.CREATE_ITEM_GROUP_REQUEST;
import static uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtils.CREATE_ITEM_GROUP_RESPONSE;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.Constants.REQUEST_ID_HEADER_NAME;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.PatchMediaType.APPLICATION_MERGE_PATCH_VALUE;

@RestController
public class ItemGroupController {

    private static final String PATCH_ITEM_URI =
            "${uk.gov.companieshouse.itemgroupworkflowapi.patchitem}";

    private final LoggingUtils logger;
    private final ItemGroupsService itemGroupsService;
    private final CreateItemValidator createItemValidator;

    private final PatchItemRequestValidator patchItemRequestValidator;

    public ItemGroupController(LoggingUtils logger,
                               ItemGroupsService itemGroupsService,
                               CreateItemValidator createItemValidator,
                               PatchItemRequestValidator patchItemRequestValidator) {
        this.logger = logger;
        this.itemGroupsService = itemGroupsService;
        this.createItemValidator = createItemValidator;
        this.patchItemRequestValidator = patchItemRequestValidator;
    }

    @PostMapping("${uk.gov.companieshouse.itemgroupworkflowapi.createitemgroup}")
    public ResponseEntity<Object> createItemGroup(final @RequestBody ItemGroupJsonPayload itemGroupJsonPayload) {
        List<String> errors = createItemValidator.validateCreateItemPayload(itemGroupJsonPayload);

        if (!errors.isEmpty()) {
            return buildValidationResponse(BAD_REQUEST.value(), errors, itemGroupJsonPayload);
        }

        try {
            if (itemGroupsService.doesItemGroupExist(itemGroupJsonPayload)) {
                return buildItemAlreadyExistsResponse(itemGroupJsonPayload);
            }

            final ItemGroupCreate savedItem = itemGroupsService.createItemGroup(itemGroupJsonPayload);
            return buildCreateSuccessResponse(savedItem);

        } catch (Exception ex) {
            return buildErrorResponse(BAD_REQUEST.value(), ex, itemGroupJsonPayload);
        }
    }

    @PatchMapping(path = PATCH_ITEM_URI, consumes = APPLICATION_MERGE_PATCH_VALUE)
    public ResponseEntity<Object> patchItem(
            final @RequestBody JsonMergePatch mergePatchDocument,
            final @PathVariable("itemGroupId") String itemGroupId,
            final @PathVariable("itemId") String itemId,
            final @RequestHeader(REQUEST_ID_HEADER_NAME) String requestId) {

        // TODO DCAC-78 Use structured logging
        logger.getLogger().info("patchItem(" + mergePatchDocument +
                ", " + itemGroupId + ", " + itemId + ", " + requestId + ") called.");

        final List<ApiError> errors = patchItemRequestValidator.getValidationErrors(mergePatchDocument);
        if (!errors.isEmpty()) {
            // TODO DCAC-78 Use structured logging
            //  logErrorsWithStatus(logMap, errors, BAD_REQUEST);
            logger.getLogger().error("update item request had validation errors " + errors);
            return ApiErrors.errorResponse(BAD_REQUEST, errors);
        }

        // TODO DCAC-78 Typed item, structured logging
        final var retrievedItem = itemGroupsService.getItem(itemGroupId, itemId);
        logger.getLogger().info("Retrieved item to be patched = " + retrievedItem);

        // TODO DCAC-78 Merge patch into retrieved item

        // TODO DCAC-78 Post-merge validation - is any required?

        // TODO DCAC-78 Save patched item

        // TODO DCAC-78 Build response DTO and return it as body

        return ResponseEntity.ok().build();
    }


    private ResponseEntity<Object> buildCreateSuccessResponse(final ItemGroupCreate savedItem) {
        return ResponseEntity.status(CREATED).body(savedItem);
    }

    private ResponseEntity<Object> buildValidationResponse(final int statusCode,
                                                           final List<String> errors,
                                                           final ItemGroupJsonPayload dto){
        final var map = logger.createLogMap();
        map.put(CREATE_ITEM_GROUP_REQUEST, dto);
        final ResponseEntity<Object> response = ResponseEntity.status(statusCode).body(errors);
        map.put(CREATE_ITEM_GROUP_RESPONSE, response);
        logger.getLogger().error(CREATE_ITEM_GROUP_ERROR_PREFIX + errors, map);
        return response;
    }

    private ResponseEntity<Object> buildItemAlreadyExistsResponse(final ItemGroupJsonPayload dto) {
        return ResponseEntity.status(CONFLICT).body(dto);
    }

    private ResponseEntity<Object> buildErrorResponse(final int statusCode,
                                                      final Exception ex,
                                                      final ItemGroupJsonPayload dto){
        final var map = logger.createLogMap();
        map.put(CREATE_ITEM_GROUP_REQUEST, dto);
        final ResponseEntity<Object> response = ResponseEntity.status(statusCode).body(ex.getMessage());
        map.put(CREATE_ITEM_GROUP_RESPONSE, response);
        logger.getLogger().error(CREATE_ITEM_GROUP_ERROR_PREFIX + ex.getMessage(), map);
        return response;
    }

}
