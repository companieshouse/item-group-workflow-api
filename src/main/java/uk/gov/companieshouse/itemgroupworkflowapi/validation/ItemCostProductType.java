package uk.gov.companieshouse.itemgroupworkflowapi.validation;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum ItemCostProductType {
    CERTIFICATE("productTypeName"),
    CERTIFICATE_SAME_DAY("certificate-same-day"),
    CERTIFICATE_ADDITIONAL_COPY("certificate-additional-copy"),
    MISSING_IMAGE_DELIVERY_ACCOUNTS("missing-image-delivery-accounts"),
    MISSING_IMAGE_DELIVERY_ANNUAL_RETURN("missing-image-delivery-annual-return"),
    MISSING_IMAGE_DELIVERY_APPOINTMENT("missing-image-delivery-appointment"),
    MISSING_IMAGE_DELIVERY_REGISTERED_OFFICE("missing-image-delivery-registered-office"),
    MISSING_IMAGE_DELIVERY_MORTGAGE("missing-image-delivery-mortgage"),
    MISSING_IMAGE_DELIVERY_LIQUIDATION("missing-image-delivery-liquidation"),
    MISSING_IMAGE_DELIVERY_NEW_INCORPORATION("missing-image-delivery-new-incorporation"),
    MISSING_IMAGE_DELIVERY_CHANGE_OF_NAME("missing-image-delivery-change-of-name"),
    MISSING_IMAGE_DELIVERY_CAPITAL("missing-image-delivery-capital"),
    MISSING_IMAGE_DELIVERY_MISC("missing-image-delivery-misc"),
    CERTIFIED_COPY("certified-copy"),
    CERTIFIED_COPY_SAME_DAY("certified-copy-same-day"),
    CERTIFIED_COPY_INCORPORATION("certified-copy-incorporation"),
    CERTIFIED_COPY_INCORPORATION_SAME_DAY("certified-copy-incorporation-same-day");

    private static final Map<String, ItemCostProductType> enumValues;

    static {
        enumValues = Arrays.stream(values())
            .collect(Collectors.toMap(ItemCostProductType::toString, Function.identity()));
    }

    private ItemCostProductType(String productTypeName) {
        this.productTypeName = productTypeName;
    }
    private final String productTypeName;

    public static ItemCostProductType getEnumValue(String productTypeName) {
        return productTypeName != null ? enumValues.get(productTypeName) : null;
    }

    public String getProductTypeName() {
        return productTypeName;
    }

    @Override
    public String toString() {
        return productTypeName;
    }
}
