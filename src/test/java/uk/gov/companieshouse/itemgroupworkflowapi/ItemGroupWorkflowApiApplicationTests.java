package uk.gov.companieshouse.itemgroupworkflowapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.companieshouse.itemgroupworkflowapi.config.MongoConfig;

@SpringBootTest(classes = {ItemGroupWorkflowApiApplication.class, MongoConfig.class})
class ItemGroupWorkflowApiApplicationTests {

    @SuppressWarnings("squid:S2699") // at least one assertion
    @Test
    void contextLoads() {
    }

}
