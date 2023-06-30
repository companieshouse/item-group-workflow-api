package uk.gov.companieshouse.itemgroupworkflowapi.kafka;

import org.springframework.stereotype.Component;
import scala.collection.JavaConverters;
import uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtils;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Item;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroupData;
import uk.gov.companieshouse.itemorderedcertifiedcopy.ItemOrderedCertifiedCopy;
import uk.gov.companieshouse.logging.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ItemOrderedCertifiedCopyFactory {

    private final Logger logger;

    public ItemOrderedCertifiedCopyFactory(final LoggingUtils loggingUtils) {
        this.logger = loggingUtils.getLogger();
    }

    public ItemOrderedCertifiedCopy buildMessage(final ItemGroupData groupCreated, final Item item) {
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
            logger.info("Scala classes detected in the filing history documents.");
            filingHistoryDocument = buildFilingHistoryDocumentFromOptionsScalaDocuments(options);
        } else {
            logger.info("Scala classes not detected in the filing history documents.");
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
