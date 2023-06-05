package uk.gov.companieshouse.itemgroupworkflowapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static uk.gov.companieshouse.itemgroupworkflowapi.environment.EnvironmentVariablesChecker.allRequiredEnvironmentVariablesPresent;

@SpringBootApplication
public class ItemGroupWorkflowApiApplication {

    public static void main(String[] args) {
        if(allRequiredEnvironmentVariablesPresent()) {
            SpringApplication.run(ItemGroupWorkflowApiApplication.class, args);
        }
    }

}
