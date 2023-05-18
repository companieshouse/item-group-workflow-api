package uk.gov.companieshouse.itemgroupworkflowapi.logging;

import uk.gov.companieshouse.logging.Logger;

import java.util.HashMap;
import java.util.Map;

public class LoggingUtils {
    public static final String APPLICATION_NAMESPACE = "item-group-workflow-api";
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
