package uk.gov.companieshouse.itemgroupworkflowapi.integration;

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
import uk.gov.companieshouse.itemgroupworkflowapi.model.Item;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroup;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemLinks;
import uk.gov.companieshouse.itemgroupworkflowapi.service.ItemGroupProcessedProducerService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

/**
 * "Test" class re-purposed to produce {@link ItemGroupProcessed} messages to the
 * <code>item-group-processed</code> topic in Tilt. This is NOT to be run as part of an
 * automated test suite. It is for manual testing only.
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:item-group-processed-in-tilt.properties")
@SpringJUnitConfig(classes = {
    KafkaConfig.class,
    ItemGroupProcessedProducerService.class,
    ItemGroupProcessedFactory.class,
    ItemGroupProcessedInTiltProducer.Config.class
})
@SuppressWarnings("squid:S3577") // This is NOT to be run as part of an automated test suite.
class ItemGroupProcessedInTiltProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        "ItemGroupProcessedInTiltProducer");

    private static final String ORDER_NUMBER = "ORD-065216-517934";
    private static final String GROUP_ITEM = "/item-groups/IG-197316-994337/items/CCD-768116-517930";
    private static final String ITEM_ID = "CCD-768116-517930";
    private static final String STATUS = "satisfied";
    private static final String DIGITAL_DOCUMENT_LOCATION =
        "s3://document-api-images-cidev/docs/--EdB7fbldt5oujK6Nz7jZ3hGj_x6vW8Q_2gQTyjWBM/application-pdf";

    private static final Item ITEM = new Item();
    private static final ItemGroup ITEM_GROUP = new ItemGroup();

    static {
        ITEM_GROUP.getData().setOrderNumber(ORDER_NUMBER);
        final ItemLinks links = new ItemLinks();
        links.setSelf(GROUP_ITEM);
        ITEM.setLinks(links);
        ITEM.setId(ITEM_ID);
        ITEM.setStatus(STATUS);
        ITEM.setDigitalDocumentLocation(DIGITAL_DOCUMENT_LOCATION);
    }

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
