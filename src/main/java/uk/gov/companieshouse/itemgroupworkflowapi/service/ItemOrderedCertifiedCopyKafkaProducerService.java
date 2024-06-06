package uk.gov.companieshouse.itemgroupworkflowapi.service;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
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
import java.util.concurrent.CompletableFuture;

import static uk.gov.companieshouse.itemgroupworkflowapi.model.ItemKind.ITEM_CERTIFICATE;
import static uk.gov.companieshouse.itemgroupworkflowapi.model.ItemKind.ITEM_CERTIFIED_COPY;
import static uk.gov.companieshouse.itemgroupworkflowapi.model.ItemKind.ITEM_MISSING_IMAGE_DELIVERY;

@Service
public class ItemOrderedCertifiedCopyKafkaProducerService implements InitializingBean {

    private record DefaultMessageSender(Logger logger) implements MessageSender {

        @Override
            public void sendMessage(final ItemGroupData group, final Item item) {
                logger.info("NOT sending a message for item " + item.getId() + " with kind " + item.getKind() + ".",
                        getLogMap(item.getId()));
            }

        }

    private record CertifiedCopyMessageSender(KafkaTemplate<String, ItemOrderedCertifiedCopy> kafkaTemplate,
                                              Logger logger, ItemOrderedCertifiedCopyFactory certifiedCopyFactory,
                                              String itemOrderedCertifiedCopyTopic) implements MessageSender {

        @Override
            public void sendMessage(final ItemGroupData group, final Item item) {
                logger.info("Sending a message for item " + item.getId() + " with kind " + item.getKind() + ".",
                        getLogMap(item.getId()));
                final ItemOrderedCertifiedCopy message = certifiedCopyFactory.buildMessage(group, item);
                final CompletableFuture<SendResult<String, ItemOrderedCertifiedCopy>> future =
                        kafkaTemplate.send(itemOrderedCertifiedCopyTopic, message);
                future.whenComplete(new ItemOrderedCertifiedCopyKafkaProducerCallback(message, itemOrderedCertifiedCopyTopic, logger));
            }
        }

    private final KafkaTemplate<String, ItemOrderedCertifiedCopy> kafkaTemplate;
    private final Logger logger;
    private final EnumMap<ItemKind, MessageSender> senders;
    private final ItemOrderedCertifiedCopyFactory certifiedCopyFactory;

    private final String itemOrderedCertifiedCopyTopic;

    public ItemOrderedCertifiedCopyKafkaProducerService(KafkaTemplate<String,
                                ItemOrderedCertifiedCopy> kafkaTemplate,
                                LoggingUtils logger,
                                ItemOrderedCertifiedCopyFactory certifiedCopyFactory,
                                @Value("${kafka.topics.item-ordered-certified-copy}")
                                String itemOrderedCertifiedCopyTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.logger = logger.logger();
        this.senders = new EnumMap<>(ItemKind.class);
        this.certifiedCopyFactory = certifiedCopyFactory;
        this.itemOrderedCertifiedCopyTopic = itemOrderedCertifiedCopyTopic;
    }

    public void produceMessages(final ItemGroupData groupCreated) {
        groupCreated.getItems().forEach(item -> produceMessage(groupCreated, item));
    }

    @Override
    public void afterPropertiesSet() {
        senders.put(ITEM_CERTIFICATE, new DefaultMessageSender(logger));
        senders.put(ITEM_MISSING_IMAGE_DELIVERY, new DefaultMessageSender(logger));
        senders.put(ITEM_CERTIFIED_COPY,
                new CertifiedCopyMessageSender(
                        kafkaTemplate, logger, certifiedCopyFactory, itemOrderedCertifiedCopyTopic));
    }

    private void produceMessage(final ItemGroupData group, final Item item) {
        final var sender = getMessageSender(item);
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

}
