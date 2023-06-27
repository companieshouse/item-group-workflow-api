package uk.gov.companieshouse.itemgroupworkflowapi.service;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtils;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Item;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroupData;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemKind;
import uk.gov.companieshouse.itemorderedcertifiedcopy.ItemOrderedCertifiedCopy;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.companieshouse.itemgroupworkflowapi.util.Constants.TOPIC_NAME;

@Service
public class KafkaProducerService implements InitializingBean {

    private static class DefaultMessageSender implements MessageSender {

        private final LoggingUtils logger;

        DefaultMessageSender(LoggingUtils logger) {
            this.logger = logger;
        }

        @Override
        public void sendMessage(final ItemGroupData group, final Item item) {
            // TODO DCAC-68 Structured logging
            logger.getLogger().info(
                    "NOT sending a message for item " + item.getId() + " with kind " + item.getKind() + ".");
        }
    }

    private static class CertifiedCopyMessageSender implements MessageSender {

        private final KafkaTemplate<String, ItemOrderedCertifiedCopy> kafkaTemplate;
        private final LoggingUtils logger;

        CertifiedCopyMessageSender(KafkaTemplate<String, ItemOrderedCertifiedCopy> kafkaTemplate, LoggingUtils logger) {
            this.kafkaTemplate = kafkaTemplate;
            this.logger = logger;
        }

        @Override
        public void sendMessage(final ItemGroupData group, final Item item) {
            // TODO DCAC-68 Structured logging
            logger.getLogger().info(
                    "Sending a message for item " + item.getId() + " with kind " + item.getKind() + ".");
            final ItemOrderedCertifiedCopy message = buildMessage(group, item);
            // TODO DCAC-68 interrogate, log result?
            kafkaTemplate.send(TOPIC_NAME, message);
        }

        private ItemOrderedCertifiedCopy buildMessage(final ItemGroupData groupCreated, final Item item) {
            // TODO DCAC-68 Plumb in each field correctly and safely.
            return ItemOrderedCertifiedCopy.newBuilder()
                    .setOrderNumber(groupCreated.getOrderNumber())
                    .setItemId(item.getId())
                    .setCompanyName(item.getCompanyName())
                    .setCompanyNumber(item.getCompanyNumber())
                    .setFilingHistoryId("TODO DCAC-68")
                    .setFilingHistoryType("TODO DCAC-68")
                    .setGroupItem(item.getLinks().getSelf())
                    .setFilingHistoryDescription("TODO DCAC-68")
                    .setFilingHistoryDescriptionValues(
                            Map.of("TODO DCAC-68 field 1", "TODO DCAC-68 field 1 value"))
                    .build();
        }
    }

    private final KafkaTemplate<String, ItemOrderedCertifiedCopy> kafkaTemplate;
    private final LoggingUtils logger;
    private final Map<ItemKind, MessageSender> senders;

    public KafkaProducerService(KafkaTemplate<String,
                           ItemOrderedCertifiedCopy> kafkaTemplate,
                                LoggingUtils logger) {
        this.kafkaTemplate = kafkaTemplate;
        this.logger = logger;
        this.senders = new HashMap<>();
    }

    public void produceMessages(final ItemGroupData groupCreated) {
        groupCreated.getItems().stream().forEach(item -> produceMessage(groupCreated, item));
    }

    @Override
    public void afterPropertiesSet() {
        senders.put(ItemKind.ITEM_CERTIFICATE, new DefaultMessageSender(logger));
        senders.put(ItemKind.ITEM_MISSING_IMAGE_DELIVERY, new DefaultMessageSender(logger));
        senders.put(ItemKind.ITEM_CERTIFIED_COPY, new CertifiedCopyMessageSender(kafkaTemplate, logger));
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
