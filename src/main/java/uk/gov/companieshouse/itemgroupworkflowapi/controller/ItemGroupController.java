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
import uk.gov.companieshouse.itemgroupworkflowapi.validation.PatchItemRequestValidator;
import uk.gov.companieshouse.logging.util.DataMap;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.Constants.REQUEST_ID_HEADER_NAME;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.PatchMediaType.APPLICATION_MERGE_PATCH_VALUE;


@RestController
public class ItemGroupController {

    private static final String PATCH_ITEM_URI =
            "${uk.gov.companieshouse.itemgroupworkflowapi.patchitem}";

    public static final String REQUEST_ID_LOG_KEY = "request_id";
    public static final String CREATE_ITEM_GROUP_REQUEST = "create_item_group: request";
    public static final String CREATE_ITEM_GROUP_RESPONSE = "create_item_group: response";
    public static final String CREATE_ITEM_GROUP_CREATED = "create_item_group: created";
    public static final String CREATE_ITEM_GROUP_ERROR_PREFIX = "create_item_group: error";
    public static final String CREATE_ITEM_GROUP_VALIDATION_PREFIX = "create_item_group: validation failed";
    public static final String ITEM_GROUP_ALREADY_EXISTS = "create_item_group: already exists";
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

    /**
     * Patches a partial update to the item group item identified. Requires the presence of a
     * <code>Content-Type=application/merge-patch+json</code> header in the request.
     * @param mergePatchDocument the JSON payload detailing the new field values for the item
     * @param itemGroupId identifies the item group
     * @param itemId identifies the item within the group
     * @param requestId the <code>X-Request-ID</code> header value used to identify each request in logs
     * @return the updated item, or an error response
     */
    @PatchMapping(path = PATCH_ITEM_URI, consumes = APPLICATION_MERGE_PATCH_VALUE)
    public ResponseEntity<Object> patchItem(
            final @RequestBody JsonMergePatch mergePatchDocument,
            final @PathVariable("itemGroupId") String itemGroupId,
            final @PathVariable("itemId") String itemId,
            final @RequestHeader(REQUEST_ID_HEADER_NAME) String requestId) {

        logger.getLogger().info("patchItem(" + mergePatchDocument +
                ", " + itemGroupId + ", " + itemId + ", " + requestId + ") called.",
                getLogMap(itemGroupId, itemId, requestId));

        final List<ApiError> errors = patchItemRequestValidator.getValidationErrors(mergePatchDocument);
        if (!errors.isEmpty()) {
            logger.getLogger().error("Patch item request had validation errors " + errors,
                    getLogMap(itemGroupId, itemId, requestId, errors));
            return ApiErrors.errorResponse(BAD_REQUEST, errors);
        }

        final var itemRetrieved = itemGroupsService.getItem(itemGroupId, itemId);
        logger.getLogger().info("Retrieved item to be patched = " + itemRetrieved,
                getLogMap(itemGroupId, itemId, requestId));

        final var patchedItem = patcher.mergePatch(mergePatchDocument, itemRetrieved, Item.class);
        logger.getLogger().info("Patched item = " + patchedItem, getLogMap(itemGroupId, itemId, requestId));

        itemGroupsService.updateItem(itemGroupId, itemId, patchedItem);

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

    private Map<String, Object> getLogMap(final String itemGroupId, final String itemId, final String requestId) {
        return new DataMap.Builder()
                .itemGroupId(itemGroupId)
                .itemId(itemId)
                .requestId(requestId)
                .build()
                .getLogMap();
    }

    private Map<String, Object> getLogMap(final String itemGroupId,
                                          final String itemId,
                                          final String requestId,
                                          final List<ApiError> errors) {
        return new DataMap.Builder()
                .itemGroupId(itemGroupId)
                .itemId(itemId)
                .requestId(requestId)
                .errors(errors.stream().map(ApiError::toString).collect(Collectors.toList()))
                .build()
                .getLogMap();
    }

}