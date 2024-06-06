package uk.gov.companieshouse.itemgroupworkflowapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka
class ItemGroupWorkflowApiApplicationTests {

    @SuppressWarnings("squid:S2699") // at least one assertion
    @Test
    void contextLoads() {
    }

}
