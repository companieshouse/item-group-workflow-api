package uk.gov.companieshouse.itemgroupworkflowapi.controller;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtils.CREATE_ITEM_GROUP_ERROR_PREFIX;
import static uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtils.CREATE_ITEM_GROUP_REQUEST;
import static uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtils.CREATE_ITEM_GROUP_RESPONSE;
import static uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtils.ITEM_GROUP_ALREADY_EXISTS;
import static uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtilsConfiguration.REQUEST_ID_HEADER_NAME;
import static uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtilsConfiguration.REQUEST_ID_LOG_KEY;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroup;
import uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtils;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroupData;
import uk.gov.companieshouse.itemgroupworkflowapi.service.ItemGroupsService;
import uk.gov.companieshouse.itemgroupworkflowapi.validation.ItemGroupsValidator;
import uk.gov.companieshouse.logging.util.DataMap;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
public class ItemGroupController {
    private final LoggingUtils logger;
    private final ItemGroupsService itemGroupsService;
    private final ItemGroupsValidator itemGroupsValidator;

    public ItemGroupController(LoggingUtils logger, ItemGroupsService itemGroupsService, ItemGroupsValidator itemGroupsValidator) {
        this.logger = logger;
        this.itemGroupsService = itemGroupsService;
        this.itemGroupsValidator = itemGroupsValidator;
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

    private void logRequestId(String requestId) {
        Map<String, Object> logMap = logger.createLogMap();
        logMap.put(REQUEST_ID_LOG_KEY, requestId);
        logger.getLogger().info("create item group request id", logMap);
    }

    private ResponseEntity<Object> buildCreateSuccessResponse(final ItemGroup savedItem) {
        DataMap dataMap = new DataMap.Builder()
            .orderId(savedItem.getData().getOrderNumber())
            .build();

        logger.getLogger().error(ITEM_GROUP_ALREADY_EXISTS + " " + dataMap.getLogMap());
        return ResponseEntity.status(CREATED).body(savedItem);
    }

    private ResponseEntity<Object> buildValidationResponse(final List<String> errors,
                                                           final ItemGroupData itemGroupData) {
        final var map = logger.createLogMap();
        map.put(CREATE_ITEM_GROUP_REQUEST, itemGroupData);

        final ResponseEntity<Object> response = ResponseEntity.status(BAD_REQUEST).body(errors);

        map.put(CREATE_ITEM_GROUP_RESPONSE, response);
        logger.getLogger().error(CREATE_ITEM_GROUP_ERROR_PREFIX + errors, map);
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
        final var map = logger.createLogMap();
        map.put(CREATE_ITEM_GROUP_REQUEST, itemGroupData);

        final ResponseEntity<Object> response = ResponseEntity.status(BAD_REQUEST).body(ex.getMessage());

        map.put(CREATE_ITEM_GROUP_RESPONSE, response);
        logger.getLogger().error(CREATE_ITEM_GROUP_ERROR_PREFIX + ex.getMessage(), map);
        return response;
    }
}