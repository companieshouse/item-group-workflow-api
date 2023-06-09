package uk.gov.companieshouse.itemgroupworkflowapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtils;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroup;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroupData;
import uk.gov.companieshouse.itemgroupworkflowapi.service.ItemGroupsService;
import uk.gov.companieshouse.itemgroupworkflowapi.validation.ItemGroupsValidator;
import uk.gov.companieshouse.logging.util.DataMap;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
public class ItemGroupController {
    public static final String REQUEST_ID_HEADER_NAME = "X-Request-ID";
    public static final String REQUEST_ID_PREFIX = "create_item_group: request_id";
    public static final String CREATE_ITEM_GROUP_CREATED_PREFIX = "create_item_group: created";
    public static final String CREATE_ITEM_GROUP_ERROR_PREFIX = "create_item_group: error";
    public static final String CREATE_ITEM_GROUP_VALIDATION_PREFIX = "create_item_group: validation failed";
    public static final String ITEM_GROUP_ALREADY_EXISTS_PREFIX = "create_item_group: already exists";
    private final LoggingUtils logger;
    private final ItemGroupsService itemGroupsService;
    private final ItemGroupsValidator itemGroupsValidator;

    public ItemGroupController(LoggingUtils logger, ItemGroupsService itemGroupsService, ItemGroupsValidator itemGroupsValidator) {
        this.logger = logger;
        this.itemGroupsService = itemGroupsService;
        this.itemGroupsValidator = itemGroupsValidator;
    }
    /**
     * Create item group from ItemGroupData<p>
     * POST RequestID logged<p>
     * Validation performed on item group, on failure return BAD_REQUEST<p>
     * Item group already exists check performed, if already exists return CONFLICT<p>
     * Item group created, on success return CREATED, on error return BAD_REQUEST<p>
     */
    @PostMapping("${uk.gov.companieshouse.itemgroupworkflowapi.createitemgroup}")
    public ResponseEntity<Object> createItemGroup(final @RequestHeader(REQUEST_ID_HEADER_NAME) String requestId,
                                                  final @RequestBody ItemGroupData itemGroupData) {
        logRequestId(requestId);
        List<String> errors = itemGroupsValidator.validateCreateItemData(itemGroupData);

        if (!errors.isEmpty()) {
            return buildValidationResponse(requestId, errors);
        }

        try {
            if (itemGroupsService.doesItemGroupExist(itemGroupData))
                return buildItemAlreadyExistsResponse(requestId, itemGroupData);

            final ItemGroup savedItem = itemGroupsService.createItemGroup(itemGroupData);
            return buildCreateSuccessResponse(requestId, savedItem);
        }
        catch (Exception ex) {
            return buildErrorResponse(requestId, ex, itemGroupData);
        }
    }

    private void logRequestId(String requestId) {
        DataMap dataMap = new DataMap.Builder()
            .requestId(requestId)
            .build();

        logger.getLogger().info(REQUEST_ID_PREFIX, dataMap.getLogMap());
    }
    /**
     * @return ResponseEntity.status(CREATED)
     */
    private ResponseEntity<Object> buildCreateSuccessResponse(String requestId,
                                                              final ItemGroup savedItem) {
        DataMap dataMap = new DataMap.Builder()
            .xRequestId(requestId)
            .orderId(savedItem.getData().getOrderNumber())
            .build();

        logger.getLogger().info(CREATE_ITEM_GROUP_CREATED_PREFIX, dataMap.getLogMap());
        return ResponseEntity.status(CREATED).body(savedItem);
    }
    /**
     * @return ResponseEntity.status(BAD_REQUEST)
     */
    private ResponseEntity<Object> buildValidationResponse(String requestId,
                                                           final List<String> errors) {
        DataMap dataMap = new DataMap.Builder()
            .xRequestId(requestId)
            .errors(errors)
            .build();

        logger.getLogger().error(CREATE_ITEM_GROUP_VALIDATION_PREFIX, dataMap.getLogMap());
        return ResponseEntity.status(BAD_REQUEST).body(errors);
    }
    /**
     * @return ResponseEntity.status(CONFLICT)
     */
    private ResponseEntity<Object> buildItemAlreadyExistsResponse(String requestId,
                                                                  final ItemGroupData itemGroupData) {
        DataMap dataMap = new DataMap.Builder()
            .xRequestId(requestId)
            .orderId(itemGroupData.getOrderNumber())
            .build();

        logger.getLogger().error(ITEM_GROUP_ALREADY_EXISTS_PREFIX, dataMap.getLogMap());
        return ResponseEntity.status(CONFLICT).body(itemGroupData);
    }
    /**
     * @return ResponseEntity.status(BAD_REQUEST)
     */
    private ResponseEntity<Object> buildErrorResponse(String requestId,
                                                      final Exception ex,
                                                      final ItemGroupData itemGroupData) {
        DataMap dataMap = new DataMap.Builder()
            .xRequestId(requestId)
            .orderId(itemGroupData.getOrderNumber())
            .message(ex.getMessage())
            .build();

        logger.getLogger().error(CREATE_ITEM_GROUP_ERROR_PREFIX, dataMap.getLogMap());
        return ResponseEntity.status(BAD_REQUEST).body(ex.getMessage());
    }
}