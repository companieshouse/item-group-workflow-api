package uk.gov.companieshouse.itemgroupworkflowapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.itemgroupworkflowapi.exception.MongoOperationException;
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
    public static final String X_REQUEST_ID_HEADER_NAME = "X-Request-ID";
    public static final String REQUEST_ID_PREFIX = "create_item_group: request_id";
    public static final String CREATE_ITEM_GROUP_CREATED_PREFIX = "create_item_group: created";
    public static final String CREATE_ITEM_GROUP_ERROR_PREFIX = "create_item_group: error";
    public static final String CREATE_ITEM_GROUP_VALIDATION_PREFIX = "create_item_group: validation failed";
    public static final String CREATE_ITEM_GROUP_ALREADY_EXISTS_PREFIX = "create_item_group: already exists";
    private final LoggingUtils logger;
    private final ItemGroupsService itemGroupsService;
    private final ItemGroupsValidator itemGroupsValidator;

    public ItemGroupController(LoggingUtils logger,
                               ItemGroupsService itemGroupsService,
                               ItemGroupsValidator itemGroupsValidator) {
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
    @PostMapping(path = "${uk.gov.companieshouse.itemgroupworkflowapi.createitemgroup}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createItemGroup(final @RequestHeader(X_REQUEST_ID_HEADER_NAME) String xRequestId,
                                                  final @RequestBody ItemGroupData itemGroupData) {
        logRequestId(xRequestId);
        List<String> errors = itemGroupsValidator.validateCreateItemData(itemGroupData);

        if (!errors.isEmpty()) {
            return buildValidationResponse(xRequestId, errors);
        }

        try {
            if (itemGroupsService.doesItemGroupExist(itemGroupData))
                return buildItemAlreadyExistsResponse(xRequestId, itemGroupData);

            final ItemGroupData savedItemGroup = itemGroupsService.createItemGroup(itemGroupData);
            return buildCreateSuccessResponse(xRequestId, savedItemGroup);
        }
        catch(IllegalArgumentException | MongoOperationException serverFailureException) {
            return buildServerErrorResponse(xRequestId, serverFailureException, itemGroupData, INTERNAL_SERVER_ERROR);
        } catch(Exception errorException) {
            return buildServerErrorResponse(xRequestId, errorException, itemGroupData, SERVICE_UNAVAILABLE);
        }
    }

    private void logRequestId(String xRequestId) {
        DataMap dataMap = new DataMap.Builder()
            .requestId(xRequestId)
            .build();

        logger.getLogger().info(REQUEST_ID_PREFIX, dataMap.getLogMap());
    }
    /**
     * @return HttpStatus.CREATED
     */
    private ResponseEntity<Object> buildCreateSuccessResponse(String xRequestId,
                                                              final ItemGroupData savedItem) {
        DataMap dataMap = new DataMap.Builder()
            .requestId(xRequestId)
            .orderId(savedItem.getOrderNumber())
            .build();

        logger.getLogger().info(CREATE_ITEM_GROUP_CREATED_PREFIX, dataMap.getLogMap());
        return ResponseEntity.status(CREATED).body(savedItem);
    }
    /**
     * @return HttpStatus.BAD_REQUEST
     */
    private ResponseEntity<Object> buildValidationResponse(String xRequestId,
                                                           final List<String> errors) {
        DataMap dataMap = new DataMap.Builder()
            .requestId(xRequestId)
            .errors(errors)
            .build();

        logger.getLogger().error(CREATE_ITEM_GROUP_VALIDATION_PREFIX, dataMap.getLogMap());
        return ResponseEntity.status(BAD_REQUEST).body(errors);
    }
    /**
     * @return HttpStatus.CONFLICT
     */
    private ResponseEntity<Object> buildItemAlreadyExistsResponse(String xRequestId,
                                                                  final ItemGroupData itemGroupData) {
        DataMap dataMap = new DataMap.Builder()
            .requestId(xRequestId)
            .orderId(itemGroupData.getOrderNumber())
            .build();

        logger.getLogger().error(CREATE_ITEM_GROUP_ALREADY_EXISTS_PREFIX, dataMap.getLogMap());
        return ResponseEntity.status(CONFLICT).body(itemGroupData);
    }
    /**
     * @return HttpStatus.BAD_REQUEST
     */
    private ResponseEntity<Object> buildErrorResponse(String xRequestId,
                                                      final Exception ex,
                                                      final ItemGroupData itemGroupData) {
        DataMap dataMap = new DataMap.Builder()
            .requestId(xRequestId)
            .orderId(itemGroupData.getOrderNumber())
            .message(ex.getMessage())
            .build();

        logger.getLogger().error(CREATE_ITEM_GROUP_ERROR_PREFIX, dataMap.getLogMap());
        return ResponseEntity.status(BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * @return Stand-in for global exception handler.
     */
    private ResponseEntity<Object> buildServerErrorResponse(
            String xRequestId,
            final Exception ex,
            final ItemGroupData itemGroupData,
            HttpStatus httpStatus) {
        DataMap dataMap = new DataMap.Builder()
                .requestId(xRequestId)
                .orderId(itemGroupData.getOrderNumber())
                .message(ex.getMessage())
                .status(httpStatus.toString())
                .build();

        logger.getLogger().error(CREATE_ITEM_GROUP_ERROR_PREFIX, dataMap.getLogMap());
        return ResponseEntity.status(httpStatus).body(ex.getMessage());
    }
}