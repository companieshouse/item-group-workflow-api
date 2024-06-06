package uk.gov.companieshouse.itemgroupworkflowapi.model;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum ItemKind {
    ITEM_CERTIFICATE("item#certificate"),
    ITEM_CERTIFIED_COPY("item#certified-copy"),
    ITEM_MISSING_IMAGE_DELIVERY("item#missing-image-delivery");

    private static final Map<String, ItemKind> enumValues;

    static {
        enumValues = Arrays.stream(values())
            .collect(Collectors.toMap(ItemKind::toString, Function.identity()));
    }

    ItemKind(String itemKindName) {
        this.itemKindName = itemKindName;
    }
    private final String itemKindName;

    public static ItemKind getEnumValue(String itemKindName) {
        return itemKindName != null ? enumValues.get(itemKindName) : null;
    }

    @Override
    public String toString() {
        return itemKindName;
    }
}
