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

import java.util.HashMap;
import java.util.Map;

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
            // TODO DCAC-68 Structured logging
            logger.info("NOT sending a message for item " + item.getId() + " with kind " + item.getKind() + ".");
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
            // TODO DCAC-68 Structured logging for all logging here
            logger.info("Sending a message for item " + item.getId() + " with kind " + item.getKind() + ".");
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
                                    partition + " with offset " + offset + ".");
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
    private final Map<ItemKind, MessageSender> senders;
    private final ItemOrderedCertifiedCopyFactory certifiedCopyFactory;

    public KafkaProducerService(KafkaTemplate<String,
                                ItemOrderedCertifiedCopy> kafkaTemplate,
                                LoggingUtils logger,
                                ItemOrderedCertifiedCopyFactory certifiedCopyFactory) {
        this.kafkaTemplate = kafkaTemplate;
        this.logger = logger.getLogger();
        this.senders = new HashMap<>();
        this.certifiedCopyFactory = certifiedCopyFactory;
    }

    public void produceMessages(final ItemGroupData groupCreated) {
        groupCreated.getItems().stream().forEach(item -> produceMessage(groupCreated, item));
    }

    @Override
    public void afterPropertiesSet() {
        senders.put(ItemKind.ITEM_CERTIFICATE, new DefaultMessageSender(logger));
        senders.put(ItemKind.ITEM_MISSING_IMAGE_DELIVERY, new DefaultMessageSender(logger));
        senders.put(ItemKind.ITEM_CERTIFIED_COPY, new CertifiedCopyMessageSender(kafkaTemplate, logger, certifiedCopyFactory));
    }

    private void produceMessage(final ItemGroupData group, final Item item) {
        // TODO DCAC-68 Consider whether lookup could fail
        final MessageSender sender = getMessageSender(item);
        sender.sendMessage(group, item);
    }

    private MessageSender getMessageSender(final Item item) {
        return senders.get(ItemKind.getEnumValue(item.getKind()));
    }

}
