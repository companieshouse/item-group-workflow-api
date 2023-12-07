package uk.gov.companieshouse.itemgroupworkflowapi.kafka;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.itemgroupprocessedsend.ItemGroupProcessedSend;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Item;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroup;

@Component
// TODO DCAC-80 Use ItemGroupProcessed.
public class ItemGroupProcessedFactory {

    public ItemGroupProcessedSend buildMessage(final Item item, final ItemGroup itemGroup) {
        return ItemGroupProcessedSend.newBuilder()
            .setOrderNumber(itemGroup.getData().getOrderNumber())
            .setGroupItem(item.getLinks().getSelf()) // TODO DCAC-80 Is this right?
            .setItem(buildAvroItem(item))
            .build();
    }

    private uk.gov.companieshouse.itemgroupprocessedsend.Item buildAvroItem(final Item item) {
        return uk.gov.companieshouse.itemgroupprocessedsend.Item.newBuilder()
            .setId(item.getId())
            .setStatus(item.getStatus())
            .setDigitalDocumentLocation(item.getDigitalDocumentLocation())
            .build();
    }

}
