package uk.gov.companieshouse.itemgroupworkflowapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.observation.OpenTelemetryServerRequestObservationConvention;
import org.springframework.http.server.observation.ServerRequestObservationConvention;

@Configuration
public class OpenTelemetryObservationConfig {

    @Bean
    ServerRequestObservationConvention serverRequestObservationConvention() {
        return new OpenTelemetryServerRequestObservationConvention();
    }

}
