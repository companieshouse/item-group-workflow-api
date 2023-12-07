package uk.gov.companieshouse.itemgroupworkflowapi.service;

import java.util.Collections;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFutureCallback;
import uk.gov.companieshouse.itemgroupprocessedsend.ItemGroupProcessedSend;
import uk.gov.companieshouse.itemgroupworkflowapi.kafka.ItemGroupProcessedFactory;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Item;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroup;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.util.DataMap;

// TODO DCAC-80 Use ItemGroupProcessed.
@Service
public class ItemGroupProcessedProducerService {

    private final KafkaTemplate<String, ItemGroupProcessedSend> kafkaTemplate;
    private final Logger logger;
    private final ItemGroupProcessedFactory itemGroupProcessedFactory;

    private final String itemGroupProcessedTopic;

    public ItemGroupProcessedProducerService(KafkaTemplate<String, ItemGroupProcessedSend> kafkaTemplate,
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

        // TODO DCAC-80 Check utility of all of these log messages

        final var orderNumber = itemGroup.getData().getOrderNumber();
        logger.info("Sending an ItemGroupProcessed message for for order number "
                + orderNumber + ", group item URI " + updatedItem.getLinks().getSelf() + ".",
            getLogMap(orderNumber, itemGroup.getId(), updatedItem.getId()));

        final var message = itemGroupProcessedFactory.buildMessage(updatedItem, itemGroup);
        final var future = kafkaTemplate.send(itemGroupProcessedTopic, message);
        future.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onSuccess(SendResult<String, ItemGroupProcessedSend> result) {
                final var metadata =  result.getRecordMetadata();
                final var partition = metadata.partition();
                final var offset = metadata.offset();
                logger.info("Message " + message + " delivered to topic " + itemGroupProcessedTopic
                                + " on partition " + partition + " with offset " + offset + ".",
                        getLogMap(message.getGroupItem(),
                                  message.getOrderNumber(),
                            itemGroupProcessedTopic,
                                  partition,
                                  offset));
            }

            @Override
            public void onFailure(Throwable ex) {
                logger.error("Unable to deliver message " + message + ". Error: " + ex.getMessage() + ".",
                        getLogMap(ex.getMessage()));
            }

        });
    }

    private static Map<String, Object> getLogMap(final String groupItem,
                                                 final String orderNumber,
                                                 final String topic,
                                                 final int partition,
                                                 final long offset) {
        return new DataMap.Builder()
                .groupItem(groupItem)
                .orderId(orderNumber)
                .topic(topic)
                .partition(partition)
                .offset(offset)
                .build()
                .getLogMap();
    }

    private static Map<String, Object> getLogMap(final String error) {
        return new DataMap.Builder()
                .errors(Collections.singletonList(error))
                .build()
                .getLogMap();
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
