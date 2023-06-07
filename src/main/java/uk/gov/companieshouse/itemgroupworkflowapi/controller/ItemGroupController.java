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
import uk.gov.companieshouse.itemgroupworkflowapi.model.Item;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroup;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroupData;
import uk.gov.companieshouse.itemgroupworkflowapi.service.ItemGroupsService;
import uk.gov.companieshouse.itemgroupworkflowapi.util.PatchMerger;
import uk.gov.companieshouse.itemgroupworkflowapi.validation.ItemGroupsValidator;
import uk.gov.companieshouse.itemgroupworkflowapi.validator.PatchItemRequestValidator;
import uk.gov.companieshouse.logging.util.DataMap;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtilsConfiguration.CREATE_ITEM_GROUP_CREATED;
import static uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtilsConfiguration.CREATE_ITEM_GROUP_ERROR_PREFIX;
import static uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtilsConfiguration.CREATE_ITEM_GROUP_REQUEST;
import static uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtilsConfiguration.CREATE_ITEM_GROUP_RESPONSE;
import static uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtilsConfiguration.CREATE_ITEM_GROUP_VALIDATION_PREFIX;
import static uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtilsConfiguration.ITEM_GROUP_ALREADY_EXISTS;
import static uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtilsConfiguration.REQUEST_ID_LOG_KEY;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.Constants.REQUEST_ID_HEADER_NAME;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.PatchMediaType.APPLICATION_MERGE_PATCH_VALUE;


@RestController
public class ItemGroupController {

    private static final String PATCH_ITEM_URI =
            "${uk.gov.companieshouse.itemgroupworkflowapi.patchitem}";

    private final LoggingUtils logger;
    private final ItemGroupsService itemGroupsService;
    private final ItemGroupsValidator itemGroupsValidator;
    private final PatchItemRequestValidator patchItemRequestValidator;
    private final PatchMerger patcher;

    public ItemGroupController(LoggingUtils logger,
                               ItemGroupsService itemGroupsService,
                               ItemGroupsValidator itemGroupsValidator,
                               PatchItemRequestValidator patchItemRequestValidator,
                               PatchMerger patcher) {
        this.logger = logger;
        this.itemGroupsService = itemGroupsService;
        this.itemGroupsValidator = itemGroupsValidator;
        this.patchItemRequestValidator = patchItemRequestValidator;
        this.patcher = patcher;
    }

    @PostMapping("${uk.gov.companieshouse.itemgroupworkflowapi.createitemgroup}")
    public ResponseEntity<Object> createItemGroup(final @RequestHeader(REQUEST_ID_HEADER_NAME) String requestId,
                                                  final @RequestBody ItemGroupData itemGroupData) {
        logRequestId(requestId);
        List<String> errors = itemGroupsValidator.validateCreateItemPayload(itemGroupData);

        if (!errors.isEmpty()) {
            return buildValidationResponse(errors, itemGroupData);
        }

        try {
            if (itemGroupsService.doesItemGroupExist(itemGroupData)) {
                return buildItemAlreadyExistsResponse(itemGroupData);
            }

            final ItemGroup savedItem = itemGroupsService.createItemGroup(itemGroupData);
            return buildCreateSuccessResponse(savedItem);

        } catch (Exception ex) {
            return buildErrorResponse(ex, itemGroupData);
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

        // TODO DCAC-78 Use structured logging
        final var itemRetrieved = itemGroupsService.getItem(itemGroupId, itemId);
        logger.getLogger().info("Retrieved item to be patched = " + itemRetrieved);

        final var patchedItem = patcher.mergePatch(mergePatchDocument, itemRetrieved, Item.class);
        logger.getLogger().info("Patched item = " + patchedItem);

        // TODO DCAC-78 Post-merge validation - is any required?

        itemGroupsService.updateItem(itemGroupId, itemId, patchedItem);

        // TODO DCAC-78 Build response DTO and return it as body?

        return ResponseEntity.ok().body(patchedItem);
    }

    private void logRequestId(String requestId) {
        Map<String, Object> logMap = logger.createLogMap();
        logMap.put(REQUEST_ID_LOG_KEY, requestId);
        logger.getLogger().info("create item group: request id", logMap);
    }

    private ResponseEntity<Object> buildCreateSuccessResponse(final ItemGroup savedItem) {
        DataMap dataMap = new DataMap.Builder()
            .orderId(savedItem.getData().getOrderNumber())
            .build();

        logger.getLogger().info(CREATE_ITEM_GROUP_CREATED, dataMap.getLogMap());
        return ResponseEntity.status(CREATED).body(savedItem);
    }

    private ResponseEntity<Object> buildValidationResponse(final List<String> errors,
                                                           final ItemGroupData itemGroupData) {
        final var map = logger.createLogMap();
        map.put(CREATE_ITEM_GROUP_REQUEST, itemGroupData);

        final ResponseEntity<Object> response = ResponseEntity.status(BAD_REQUEST).body(errors);

        map.put(CREATE_ITEM_GROUP_RESPONSE, response);
        logger.getLogger().error(CREATE_ITEM_GROUP_VALIDATION_PREFIX + " " + errors, map);
        return response;
    }

    private ResponseEntity<Object> buildItemAlreadyExistsResponse(final ItemGroupData itemGroupData) {
        DataMap dataMap = new DataMap.Builder()
            .orderId(itemGroupData.getOrderNumber())
            .build();

        logger.getLogger().error(ITEM_GROUP_ALREADY_EXISTS + " " + dataMap.getLogMap());
        return ResponseEntity.status(CONFLICT).body(itemGroupData);
    }

    private ResponseEntity<Object> buildErrorResponse(final Exception ex,
                                                      final ItemGroupData itemGroupData){
        final ResponseEntity<Object> response = ResponseEntity.status(BAD_REQUEST).body(ex.getMessage());

        final var map = logger.createLogMap();
        map.put(CREATE_ITEM_GROUP_REQUEST, itemGroupData);
        logger.getLogger().error(CREATE_ITEM_GROUP_ERROR_PREFIX + ex.getMessage(), map);
        return response;
    }
}