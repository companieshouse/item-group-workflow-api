package uk.gov.companieshouse.itemgroupworkflowapi.validation;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum ItemDescriptionIdentifier {
    CERTIFICATE("certificate"),
    CERTIFIED_COPY("certified-copy"),
    MISSING_IMAGE_DELIVERY("missing-image-delivery");

    private static final Map<String, ItemDescriptionIdentifier> enumValues;

    static {
        enumValues = Arrays.stream(values())
            .collect(Collectors.toMap(ItemDescriptionIdentifier::toString, Function.identity()));
    }

    private ItemDescriptionIdentifier(String identifierName) {
        this.identifierName = identifierName;
    }
    private final String identifierName;

    public static ItemDescriptionIdentifier getEnumValue(String identifierName) {
        return identifierName != null ? enumValues.get(identifierName) : null;
    }

    public String getIdentifierName() {
        return identifierName;
    }

    @Override
    public String toString() {
        return identifierName;
    }
}
