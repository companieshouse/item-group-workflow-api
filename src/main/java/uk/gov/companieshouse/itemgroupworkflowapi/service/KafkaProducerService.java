package uk.gov.companieshouse.itemgroupworkflowapi.service;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtils;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Item;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroupData;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemKind;
import uk.gov.companieshouse.itemorderedcertifiedcopy.ItemOrderedCertifiedCopy;
import uk.gov.companieshouse.logging.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import scala.collection.JavaConverters;

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

        CertifiedCopyMessageSender(KafkaTemplate<String, ItemOrderedCertifiedCopy> kafkaTemplate, Logger logger) {
            this.kafkaTemplate = kafkaTemplate;
            this.logger = logger;
        }

        @Override
        public void sendMessage(final ItemGroupData group, final Item item) {
            // TODO DCAC-68 Structured logging
            logger.info("Sending a message for item " + item.getId() + " with kind " + item.getKind() + ".");
            final ItemOrderedCertifiedCopy message = buildMessage(group, item);
            // TODO DCAC-68 interrogate, log result?
            kafkaTemplate.send(TOPIC_NAME, message);
        }

        private ItemOrderedCertifiedCopy buildMessage(final ItemGroupData groupCreated, final Item item) {
            // TODO DCAC-68 Introduce some type safety here?
            final var filingHistoryDocument = getFilingHistoryDocument(item);
            return ItemOrderedCertifiedCopy.newBuilder()
                    .setOrderNumber(groupCreated.getOrderNumber())
                    .setItemId(item.getId())
                    .setCompanyName(item.getCompanyName())
                    .setCompanyNumber(item.getCompanyNumber())
                    .setFilingHistoryId((String) filingHistoryDocument.get("filing_history_id"))
                    .setFilingHistoryType((String) filingHistoryDocument.get("filing_history_type"))
                    .setGroupItem(item.getLinks().getSelf())
                    .setFilingHistoryDescription((String) filingHistoryDocument.get("filing_history_description"))
                    .setFilingHistoryDescriptionValues((Map)
                    filingHistoryDocument.get("filing_history_description_values"))
                    .build();
        }

        private Map getFilingHistoryDocument(final Item item) {
            // TODO DCAC-68 Is is safe to assume we can always get FH details from the 1st filing history document?
            final var options = item.getItemOptions();

            // TODO DCAC-68: This Scala weirdness only seems to arise in our Spring Boot integration tests.
            // This problem might disappear if we use typed collections to create the request body?
            final Map filingHistoryDocument;
            if (options.get("filing_history_documents") instanceof scala.collection.immutable.List) {
                filingHistoryDocument = buildFilingHistoryDocumentFromOptionsScalaDocuments(options);
            } else {
                filingHistoryDocument = (Map) ((List) options.get("filing_history_documents")).get(0);
            }

            // TODO DCAC-68 Structured logging, or remove this.
            logger.info("filingHistoryDocument = " + filingHistoryDocument);
            return filingHistoryDocument;
        }

        private Map buildFilingHistoryDocumentFromOptionsScalaDocuments(final Map options) {
            final var scalaFilingHistoryDocuments =
                    (scala.collection.immutable.List) options.get("filing_history_documents");
            final var javaFilingHistoryDocuments = JavaConverters.asJava(scalaFilingHistoryDocuments);
            final var scalaFilingHistoryDocument = (scala.collection.immutable.Map) javaFilingHistoryDocuments.get(0);
            final var immutableJavaFilingHistoryDocument = JavaConverters.asJava(scalaFilingHistoryDocument);
            final var scalaDescriptionValues =
                    (scala.collection.immutable.Map)
                            immutableJavaFilingHistoryDocument.get("filing_history_description_values");
            final var javaDescriptionValues = JavaConverters.asJava(scalaDescriptionValues);
            // The put operation requires that the map in question is mutable.
            final var filingHistoryDocument = new HashMap(immutableJavaFilingHistoryDocument);
            filingHistoryDocument.put("filing_history_description_values", javaDescriptionValues);
            return filingHistoryDocument;
        }
    }

    private final KafkaTemplate<String, ItemOrderedCertifiedCopy> kafkaTemplate;
    private final Logger logger;
    private final Map<ItemKind, MessageSender> senders;

    public KafkaProducerService(KafkaTemplate<String,
                                ItemOrderedCertifiedCopy> kafkaTemplate,
                                LoggingUtils logger) {
        this.kafkaTemplate = kafkaTemplate;
        this.logger = logger.getLogger();
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
