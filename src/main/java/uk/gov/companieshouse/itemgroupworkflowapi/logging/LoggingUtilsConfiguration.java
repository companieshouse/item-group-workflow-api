package uk.gov.companieshouse.itemgroupworkflowapi.logging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Configuration
public class LoggingUtilsConfiguration {
    public static final String APPLICATION_NAMESPACE = "item-group-workflow-api";
    public static final String REQUEST_ID_LOG_KEY = "request_id";
    public static final String REQUEST_ID_HEADER_NAME = "Document-Signing-API";
    public static final String STATUS_LOG_KEY = "status";
    public static final String IDENTITY_LOG_KEY = "ERIC Identity";
    public static final String IDENTITY_TYPE_LOG_KEY = "ERIC Identity Type";
    public static final String MISSING_REQUIRED_INFO = "Required information missing";

    @Bean
    Logger getLogger(){
        return LoggerFactory.getLogger(APPLICATION_NAMESPACE);
    }

    @Bean
    public LoggingUtils getLoggingUtils(Logger logger) {
        return new LoggingUtils(logger);
    }
}
