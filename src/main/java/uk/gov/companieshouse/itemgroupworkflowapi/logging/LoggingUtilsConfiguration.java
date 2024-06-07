package uk.gov.companieshouse.itemgroupworkflowapi.logging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Configuration
public class LoggingUtilsConfiguration {
    public static final String APPLICATION_NAME_SPACE = "item-group-workflow-api";

    @Bean
    Logger getLogger(){
        return LoggerFactory.getLogger(APPLICATION_NAME_SPACE);
    }

    @Bean
    public LoggingUtils getLoggingUtils(Logger logger) {
        return new LoggingUtils(logger);
    }
}
