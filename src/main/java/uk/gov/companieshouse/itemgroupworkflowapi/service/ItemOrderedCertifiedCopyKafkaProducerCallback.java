package uk.gov.companieshouse.itemgroupworkflowapi.service;

import org.springframework.kafka.support.SendResult;
import uk.gov.companieshouse.itemorderedcertifiedcopy.ItemOrderedCertifiedCopy;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.util.DataMap;

import java.util.Map;
import java.util.function.BiConsumer;

public class ItemOrderedCertifiedCopyKafkaProducerCallback implements BiConsumer<SendResult<String, ItemOrderedCertifiedCopy>, Throwable> {

    private final ItemOrderedCertifiedCopy message;
    private final String itemOrderedCertifiedCopyTopic;
    private final Logger logger;

    public ItemOrderedCertifiedCopyKafkaProducerCallback(ItemOrderedCertifiedCopy message, String itemOrderedCertifiedCopyTopic, Logger logger) {
        this.message = message;
        this.itemOrderedCertifiedCopyTopic = itemOrderedCertifiedCopyTopic;
        this.logger = logger;
    }

    public void onSuccess(SendResult<String, ItemOrderedCertifiedCopy> result) {
        final var metadata =  result.getRecordMetadata();
        final var partition = metadata.partition();
        final var offset = metadata.offset();
        logger.info("Message " + message + " delivered to topic " + itemOrderedCertifiedCopyTopic
                        + " on partition " + partition + " with offset " + offset + ".",
                getLogMap(message.getItemId(), itemOrderedCertifiedCopyTopic, partition, offset));
    }

    public void onFailure(Throwable ex) {
        logger.error("Unable to deliver message " + message + ". Error: " + ex.getMessage() + ".");
    }

    @Override
    public void accept(SendResult<String, ItemOrderedCertifiedCopy> sendResult, Throwable throwable) {
        if(throwable != null) {
            onFailure(throwable);
        } else {
            onSuccess(sendResult);
        }
    }

    private static Map<String, Object> getLogMap(final String itemId,
                                                 final String topic,
                                                 final int partition,
                                                 final long offset) {
        return new DataMap.Builder()
                .itemId(itemId)
                .topic(topic)
                .partition(partition)
                .offset(offset)
                .build()
                .getLogMap();
    }
}
