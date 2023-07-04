package uk.gov.companieshouse.itemgroupworkflowapi.service;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import uk.gov.companieshouse.itemgroupworkflowapi.kafka.ItemOrderedCertifiedCopyFactory;
import uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtils;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Item;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroupData;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemKind;
import uk.gov.companieshouse.itemorderedcertifiedcopy.ItemOrderedCertifiedCopy;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.util.DataMap;

import java.util.EnumMap;
import java.util.Map;

import static uk.gov.companieshouse.itemgroupworkflowapi.model.ItemKind.ITEM_CERTIFICATE;
import static uk.gov.companieshouse.itemgroupworkflowapi.model.ItemKind.ITEM_CERTIFIED_COPY;
import static uk.gov.companieshouse.itemgroupworkflowapi.model.ItemKind.ITEM_MISSING_IMAGE_DELIVERY;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.Constants.TOPIC_NAME;

@Service
public class KafkaProducerService implements InitializingBean {

    private static class DefaultMessageSender implements MessageSender {

        private final Logger logger;

        DefaultMessageSender(Logger logger) {
            this.logger = logger;
        }

        @Override
        public void sendMessage(final ItemGroupData group, final Item item) {
            logger.info("NOT sending a message for item " + item.getId() + " with kind " + item.getKind() + ".",
                    getLogMap(item.getId()));
        }

    }

    private static class CertifiedCopyMessageSender implements MessageSender {

        private final KafkaTemplate<String, ItemOrderedCertifiedCopy> kafkaTemplate;
        private final Logger logger;
        private final ItemOrderedCertifiedCopyFactory certifiedCopyFactory;

        CertifiedCopyMessageSender(KafkaTemplate<String, ItemOrderedCertifiedCopy> kafkaTemplate,
                                   Logger logger,
                                   ItemOrderedCertifiedCopyFactory certifiedCopyFactory) {
            this.kafkaTemplate = kafkaTemplate;
            this.logger = logger;
            this.certifiedCopyFactory = certifiedCopyFactory;
        }

        @Override
        public void sendMessage(final ItemGroupData group, final Item item) {
            logger.info("Sending a message for item " + item.getId() + " with kind " + item.getKind() + ".",
                    getLogMap(item.getId()));
            final ItemOrderedCertifiedCopy message = certifiedCopyFactory.buildMessage(group, item);
            final ListenableFuture<SendResult<String, ItemOrderedCertifiedCopy>> future =
                    kafkaTemplate.send(TOPIC_NAME, message);
            future.addCallback(new ListenableFutureCallback<>() {
                @Override
                public void onSuccess(SendResult<String, ItemOrderedCertifiedCopy> result) {
                    final var metadata =  result.getRecordMetadata();
                    final var partition = metadata.partition();
                    final var offset = metadata.offset();
                    logger.info("Message " + message + " delivered to topic " + TOPIC_NAME + " on partition " +
                                    partition + " with offset " + offset + ".",
                            getLogMap(message.getItemId(), TOPIC_NAME, partition, offset));
                }

                @Override
                public void onFailure(Throwable ex) {
                    logger.error("Unable to deliver message " + message + ". Error: " + ex.getMessage() + ".");
                }
            });
        }

    }

    private final KafkaTemplate<String, ItemOrderedCertifiedCopy> kafkaTemplate;
    private final Logger logger;
    private final EnumMap<ItemKind, MessageSender> senders;
    private final ItemOrderedCertifiedCopyFactory certifiedCopyFactory;

    public KafkaProducerService(KafkaTemplate<String,
                                ItemOrderedCertifiedCopy> kafkaTemplate,
                                LoggingUtils logger,
                                ItemOrderedCertifiedCopyFactory certifiedCopyFactory) {
        this.kafkaTemplate = kafkaTemplate;
        this.logger = logger.getLogger();
        this.senders = new EnumMap<>(ItemKind.class);
        this.certifiedCopyFactory = certifiedCopyFactory;
    }

    public void produceMessages(final ItemGroupData groupCreated) {
        groupCreated.getItems().forEach(item -> produceMessage(groupCreated, item));
    }

    @Override
    public void afterPropertiesSet() {
        senders.put(ITEM_CERTIFICATE, new DefaultMessageSender(logger));
        senders.put(ITEM_MISSING_IMAGE_DELIVERY, new DefaultMessageSender(logger));
        senders.put(ITEM_CERTIFIED_COPY, new CertifiedCopyMessageSender(kafkaTemplate, logger, certifiedCopyFactory));
    }

    private void produceMessage(final ItemGroupData group, final Item item) {
        final MessageSender sender = getMessageSender(item);
        sender.sendMessage(group, item);
    }

    private MessageSender getMessageSender(final Item item) {
        return senders.get(ItemKind.getEnumValue(item.getKind()));
    }

    private static Map<String, Object> getLogMap(final String itemId) {
        return new DataMap.Builder()
                .itemId(itemId)
                .build()
                .getLogMap();
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
