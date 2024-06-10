package uk.gov.companieshouse.itemgroupworkflowapi.service;

import java.util.Collections;
import java.util.Map;
import java.util.function.BiConsumer;

import org.springframework.kafka.support.SendResult;
import uk.gov.companieshouse.itemgroupprocessed.ItemGroupProcessed;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.util.DataMap;

class ItemGroupProcessedProducerCallback implements
        BiConsumer<SendResult<String, ItemGroupProcessed>, Throwable> {

    private final ItemGroupProcessed message;
    private final String itemGroupProcessedTopic;
    private final Logger logger;

    public ItemGroupProcessedProducerCallback(ItemGroupProcessed message,
        String itemGroupProcessedTopic, Logger logger) {
        this.message = message;
        this.itemGroupProcessedTopic = itemGroupProcessedTopic;
        this.logger = logger;
    }

    public void onFailure(Throwable ex) {
        logger.error("Unable to deliver message " + message + " to topic " + itemGroupProcessedTopic
                + ". Error: " + ex.getMessage() + ".",
            getLogMap(message, itemGroupProcessedTopic, ex.getMessage()));
    }

    public void onSuccess(SendResult<String, ItemGroupProcessed> result) {
        final var metadata = result.getRecordMetadata();
        final var partition = metadata.partition();
        final var offset = metadata.offset();
        logger.info("Message " + message + " delivered to topic " + itemGroupProcessedTopic
                + " on partition " + partition + " with offset " + offset + ".",
            getLogMap(message,
                message.getGroupItem(),
                message.getOrderNumber(),
                itemGroupProcessedTopic,
                partition,
                offset));
    }

    private static Map<String, Object> getLogMap(final ItemGroupProcessed message,
                                                 final String topic,
                                                 final String error) {
        return new DataMap.Builder()
            .kafkaMessage(message.toString())
            .topic(topic)
            .errors(Collections.singletonList(error))
            .build()
            .getLogMap();
    }

    private static Map<String, Object> getLogMap(final ItemGroupProcessed message,
        final String groupItem,
        final String orderNumber,
        final String topic,
        final int partition,
        final long offset) {
        return new DataMap.Builder()
            .kafkaMessage(message.toString())
            .groupItem(groupItem)
            .orderId(orderNumber)
            .topic(topic)
            .partition(partition)
            .offset(offset)
            .build()
            .getLogMap();
    }

    @Override
    public void accept(SendResult<String, ItemGroupProcessed> sendResult, Throwable throwable) {
        if(throwable != null) {
            onFailure(throwable);
        } else {
            onSuccess(sendResult);
        }
    }
}
