package uk.gov.companieshouse.itemgroupworkflowapi.util;

import uk.gov.companieshouse.itemgroupworkflowapi.model.Item;

public class TestUtils {

    private TestUtils() {}

    public static uk.gov.companieshouse.itemgroupprocessed.Item buildAvroItem(final Item item) {
        return uk.gov.companieshouse.itemgroupprocessed.Item.newBuilder()
            .setId(item.getId())
            .setStatus(item.getStatus())
            .setDigitalDocumentLocation(item.getDigitalDocumentLocation())
            .build();
    }

}
