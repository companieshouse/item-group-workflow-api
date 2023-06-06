package uk.gov.companieshouse.itemgroupworkflowapi.logging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Configuration
public class LoggingUtilsConfiguration {
    public static final String APPLICATION_NAMESPACE = "item-group-workflow-api";
    public static final String REQUEST_ID_HEADER_NAME = "X-Request-ID";
    public static final String REQUEST_ID_LOG_KEY = "request_id";
    public static final String CREATE_ITEM_GROUP_REQUEST = "create_item_group: request";
    public static final String CREATE_ITEM_GROUP_RESPONSE = "create_item_group: response";
    public static final String CREATE_ITEM_GROUP_CREATED = "create_item_group: created";
    public static final String CREATE_ITEM_GROUP_ERROR_PREFIX = "create_item_group: error";
    public static final String CREATE_ITEM_GROUP_VALIDATION_PREFIX = "create_item_group: validation failed";
    public static final String ITEM_GROUP_ALREADY_EXISTS = "create_item_group: already exists";

    @Bean
    Logger getLogger(){
        return LoggerFactory.getLogger(APPLICATION_NAMESPACE);
    }

    @Bean
    public LoggingUtils getLoggingUtils(Logger logger) {
        return new LoggingUtils(logger);
    }
}
