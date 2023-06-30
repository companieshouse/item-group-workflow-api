package uk.gov.companieshouse.itemgroupworkflowapi.util;

import uk.gov.companieshouse.itemorderedcertifiedcopy.ItemOrderedCertifiedCopy;

import java.util.List;
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

    public static final Map<String, Object> CERTIFIED_COPY_ITEM_OPTIONS =
        Map.of("filing_history_documents",
            List.of(
                Map.of(
                    "filing_history_date", "2023-05-18",
                    "filing_history_description", "appoint-person-director-company-with-name-date",
                    "filing_history_description_values", Map.of(
                            "appointment_date", "2023-05-01",
                            "officer_name", "Mr Tom Sunburn"
                    ),
                    "filing_history_id", "OTYyMTM3NjgxOGFkaXF6a2N4",
                    "filing_history_type", "AP01",
                    "filing_history_cost", "50"
                )
            ),
            "delivery_method", "collection",
            "delivery_timescale", "standard"
        );
}
