package uk.gov.companieshouse.item.group.workflow.api.controller;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static uk.gov.companieshouse.item.group.workflow.api.logging.LoggingUtils.CREATE_ITEM_GROUP_ERROR_PREFIX;
import static uk.gov.companieshouse.item.group.workflow.api.logging.LoggingUtils.CREATE_ITEM_GROUP_REQUEST;
import static uk.gov.companieshouse.item.group.workflow.api.logging.LoggingUtils.CREATE_ITEM_GROUP_RESPONSE;
import static uk.gov.companieshouse.item.group.workflow.api.logging.LoggingUtils.ITEM_GROUP_ALREADY_EXISTS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.item.group.workflow.api.model.ItemGroupCreate;
import uk.gov.companieshouse.item.group.workflow.api.logging.LoggingUtils;
import uk.gov.companieshouse.item.group.workflow.api.model.ItemGroupJsonPayload;
import uk.gov.companieshouse.item.group.workflow.api.service.ItemGroupsService;
import uk.gov.companieshouse.item.group.workflow.api.validation.CreateItemValidator;
import uk.gov.companieshouse.logging.util.DataMap;

import java.util.List;
import java.util.Map;

@RestController
public class ItemGroupController {
    private final LoggingUtils logger;
    private final ItemGroupsService itemGroupsService;
    private final CreateItemValidator createItemValidator;

    public ItemGroupController(LoggingUtils logger, ItemGroupsService itemGroupsService, CreateItemValidator createItemValidator) {
        this.logger = logger;
        this.itemGroupsService = itemGroupsService;
        this.createItemValidator = createItemValidator;
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