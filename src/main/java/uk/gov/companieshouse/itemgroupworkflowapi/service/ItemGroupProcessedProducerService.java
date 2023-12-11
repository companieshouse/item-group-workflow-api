package uk.gov.companieshouse.itemgroupworkflowapi.service;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.itemgroupprocessed.ItemGroupProcessed;
import uk.gov.companieshouse.itemgroupworkflowapi.kafka.ItemGroupProcessedFactory;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Item;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroup;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.util.DataMap;

/**
 * Service that propagates the updated status of an item in an item group by producing
 * <code>item-group-processed</code> Kafka messages to be consumed by the
 * <code>item-group-status-updater</code>.
 */
@Service
public class ItemGroupProcessedProducerService {

    private final KafkaTemplate<String, ItemGroupProcessed> kafkaTemplate;
    private final Logger logger;
    private final ItemGroupProcessedFactory itemGroupProcessedFactory;

    private final String itemGroupProcessedTopic;

    public ItemGroupProcessedProducerService(
        KafkaTemplate<String, ItemGroupProcessed> kafkaTemplate,
        Logger logger,
        ItemGroupProcessedFactory itemGroupProcessedFactory,
        @Value("${kafka.topics.item-group-processed}")
        String itemGroupProcessedTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.logger = logger;
        this.itemGroupProcessedFactory = itemGroupProcessedFactory;
        this.itemGroupProcessedTopic = itemGroupProcessedTopic;
    }

    public void sendMessage(final Item updatedItem, final ItemGroup itemGroup) {
        final var orderNumber = itemGroup.getData().getOrderNumber();

        logger.info("Sending an ItemGroupProcessed message for order number "
                + orderNumber + ", group item URI " + updatedItem.getLinks().getSelf() + ".",
            getLogMap(orderNumber, itemGroup.getId(), updatedItem.getId()));

        final var message = itemGroupProcessedFactory.buildMessage(updatedItem, itemGroup);
        final var future = kafkaTemplate.send(itemGroupProcessedTopic, message);
        future.addCallback(
            new ItemGroupProcessedProducerCallback(message, itemGroupProcessedTopic, logger));
    }

    private Map<String, Object> getLogMap(
        final String orderNumber,
        final String itemGroupId,
        final String itemId) {
        return new DataMap.Builder()
            .orderId(orderNumber)
            .itemGroupId(itemGroupId)
            .itemId(itemId)
            .build()
            .getLogMap();
    }

}
