package uk.gov.companieshouse.itemgroupworkflowapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ItemGroupWorkflowApiApplication {

    public static final String NAMESPACE = "item-group-workflow-api";

    public static void main(String[] args) {
        SpringApplication.run(ItemGroupWorkflowApiApplication.class, args);
    }

}
