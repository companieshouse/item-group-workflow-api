package uk.gov.companieshouse.itemgroupworkflowapi.kafka;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtils;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Item;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroupData;
import uk.gov.companieshouse.itemorderedcertifiedcopy.ItemOrderedCertifiedCopy;
import uk.gov.companieshouse.logging.Logger;

import java.util.List;
import java.util.Map;

@Component
public class ItemOrderedCertifiedCopyFactory {

    private final Logger logger;

    public ItemOrderedCertifiedCopyFactory(final LoggingUtils loggingUtils) {
        this.logger = loggingUtils.getLogger();
    }

    public ItemOrderedCertifiedCopy buildMessage(final ItemGroupData groupCreated, final Item item) {
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
                .setFilingHistoryDescriptionValues((Map<String, String>)
                        filingHistoryDocument.get("filing_history_description_values"))
                .build();
    }

    protected Map<String, Object> getFilingHistoryDocument(final Item item) {
        final var options = item.getItemOptions();
        final var filingHistoryDocument =
                 (Map<String, Object>) ((List<Object>) options.get("filing_history_documents")).get(0);
        // TODO DCAC-68 Structured logging, or remove this.
        logger.info("filingHistoryDocument = " + filingHistoryDocument);
        return filingHistoryDocument;
    }

}
