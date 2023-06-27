package uk.gov.companieshouse.itemgroupworkflowapi.util;

import uk.gov.companieshouse.itemorderedcertifiedcopy.ItemOrderedCertifiedCopy;

import java.util.Map;

public class TestConstants {

    private TestConstants() {
    }

    // TODO DCAC-68 Come up with more representative values for fields.
    public static final ItemOrderedCertifiedCopy CERTIFIED_COPY = ItemOrderedCertifiedCopy.newBuilder()
            .setOrderNumber("ORD-152416-079544")
            .setItemId("CCD-768116-517930")
            .setGroupItem("/item-groups/IG-000216-873460/items/CCD-768116-517930")
            .setCompanyName("Test Company Limited")
            .setCompanyNumber("00006400")
            .setFilingHistoryDescription("A test filing history document")
            .setFilingHistoryId("TBD")
            .setFilingHistoryType("AM01")
            .setFilingHistoryDescriptionValues(Map.of("field 1", "field 1 value"))
            .build();

    public static final String SAME_PARTITION_KEY = "key";
}
