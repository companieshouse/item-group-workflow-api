package uk.gov.companieshouse.item.group.workflow.api.logging;

import uk.gov.companieshouse.logging.Logger;

import java.util.HashMap;
import java.util.Map;

public class LoggingUtils {
    public static final String CREATE_ITEM_GROUP_REQUEST = "create_item_group_request";
    public static final String CREATE_ITEM_GROUP_RESPONSE = "create_item_group_response";
    public static final String CREATE_ITEM_GROUP_ERROR_PREFIX = "createItemGroup: ";
    public static final String ITEM_GROUP_ALREADY_EXISTS = "createItemGroup: ";
    private final Logger logger;

    public LoggingUtils(Logger logger) {
        this.logger = logger;
    }

    public Map<String, Object> createLogMap() {
        return new HashMap<>();
    }

    public void logIfNotNull(Map<String, Object> logMap, String key, Object loggingObject) {
        if (loggingObject != null) {
            logMap.put(key, loggingObject);
        }
    }

    public Logger getLogger() {
        return logger;
    }
}
