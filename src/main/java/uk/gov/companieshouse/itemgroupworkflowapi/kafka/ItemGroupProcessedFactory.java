package uk.gov.companieshouse.itemgroupworkflowapi.kafka;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.itemgroupprocessed.ItemGroupProcessed;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Item;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroup;

@Component
public class ItemGroupProcessedFactory {

    public ItemGroupProcessed buildMessage(final Item item, final ItemGroup itemGroup) {
        return ItemGroupProcessed.newBuilder()
            .setOrderNumber(itemGroup.getData().getOrderNumber())
            .setGroupItem(item.getLinks().getSelf())
            .setItem(buildAvroItem(item))
            .build();
    }

    private uk.gov.companieshouse.itemgroupprocessed.Item buildAvroItem(final Item item) {
        return uk.gov.companieshouse.itemgroupprocessed.Item.newBuilder()
            .setId(item.getId())
            .setStatus(item.getStatus())
            .setDigitalDocumentLocation(item.getDigitalDocumentLocation())
            .build();
    }

}
