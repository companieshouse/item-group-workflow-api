package uk.gov.companieshouse.itemgroupworkflowapi.util;

import static uk.gov.companieshouse.itemgroupworkflowapi.util.TestUtils.buildAvroItem;

import java.util.List;
import java.util.Map;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Item;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroup;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemLinks;
import uk.gov.companieshouse.itemorderedcertifiedcopy.ItemOrderedCertifiedCopy;

public class TestConstants {

    private TestConstants() {
    }

    public static final String ERIC_IDENTITY_HEADER_NAME = "ERIC-Identity";
    public static final String ERIC_IDENTITY_HEADER_VALUE = "any non-empty value";
    public static final String ERIC_IDENTITY_TYPE_HEADER_VALUE = "any non-empty value";
    public static final String ERIC_AUTHORISED_ROLES_HEADER_NAME = "ERIC-Authorised-Key-Roles";
    public static final String ERIC_AUTHORISED_ROLES_HEADER_VALUE = "*";
    public static final String ERIC_IDENTITY_TYPE_HEADER_NAME = "ERIC-Identity-Type";

    public static final ItemOrderedCertifiedCopy CERTIFIED_COPY = ItemOrderedCertifiedCopy.newBuilder()
            .setOrderNumber("ORD-152416-079544")
            .setItemId("CCD-768116-517930")
            .setGroupItem("/item-groups/IG-000216-873460/items/CCD-768116-517930")
            .setCompanyName("Test Company Limited")
            .setCompanyNumber("00006400")
            .setFilingHistoryDescription("appoint-person-director-company-with-name-date")
            .setFilingHistoryId("OTYyMTM3NjgxOGFkaXF6a2N4")
            .setFilingHistoryType("AP01")
            .setFilingHistoryDescriptionValues(Map.of(
                    "appointment_date", "2023-05-01",
                    "officer_name", "Mr Tom Sunburn"))
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

    public static final String ITEM_ORDERED_CERTIFIED_COPY_TOPIC = "item-ordered-certified-copy";

    public static final String ORDER_NUMBER = "ORD-065216-517934";
    public static final String GROUP_ITEM = "/item-groups/IG-197316-994337/items/CCD-768116-517930";
    public static final String ITEM_ID = "CCD-768116-517930";
    public static final String STATUS = "satisfied";
    public static final String DIGITAL_DOCUMENT_LOCATION =
        "s3://document-api-images-cidev/docs/--EdB7fbldt5oujK6Nz7jZ3hGj_x6vW8Q_2gQTyjWBM/application-pdf";

    public static final Item ITEM = new Item();
    public static final ItemGroup ITEM_GROUP = new ItemGroup();

    public static final uk.gov.companieshouse.itemgroupprocessed.Item AVRO_ITEM;

    static {
        ITEM_GROUP.getData().setOrderNumber(ORDER_NUMBER);
        final ItemLinks links = new ItemLinks();
        links.setSelf(GROUP_ITEM);
        ITEM.setLinks(links);
        ITEM.setId(ITEM_ID);
        ITEM.setStatus(STATUS);
        ITEM.setDigitalDocumentLocation(DIGITAL_DOCUMENT_LOCATION);
        AVRO_ITEM = buildAvroItem(ITEM);
    }

}
