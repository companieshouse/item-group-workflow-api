package uk.gov.companieshouse.itemgroupworkflowapi.integration;

import static uk.gov.companieshouse.itemgroupworkflowapi.util.TestConstants.ITEM;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.TestConstants.ITEM_GROUP;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.companieshouse.itemgroupprocessed.ItemGroupProcessed;
import uk.gov.companieshouse.itemgroupworkflowapi.config.KafkaConfig;
import uk.gov.companieshouse.itemgroupworkflowapi.kafka.ItemGroupProcessedFactory;
import uk.gov.companieshouse.itemgroupworkflowapi.service.ItemGroupProcessedProducerService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

/**
 * "Test" class re-purposed to produce {@link ItemGroupProcessed} messages to the
 * <code>item-group-processed</code> topic in Tilt. This is NOT to be run as part of an
 * automated test suite. It is for manual testing only.
 */
@Disabled
@SpringBootTest
@TestPropertySource(locations = "classpath:item-group-processed-in-tilt.properties")
@SpringJUnitConfig(classes = {
    KafkaConfig.class,
    ItemGroupProcessedProducerService.class,
    ItemGroupProcessedFactory.class,
    ItemGroupProcessedInTiltProducerTest.Config.class
})
@SuppressWarnings("squid:S3577") // This is NOT to be run as part of an automated test suite.
class ItemGroupProcessedInTiltProducerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        "ItemGroupProcessedInTiltProducerTest");

    @Autowired
    private ItemGroupProcessedProducerService testProducer;

    @Configuration
    static class Config {

        @Bean
        public Logger getLogger() {
            return LOGGER;
        }

    }

    @SuppressWarnings("squid:S2699") // at least one assertion
    @Test
    void produceMessageToTilt() {
        testProducer.sendMessage(ITEM, ITEM_GROUP);
    }

}
