package uk.gov.companieshouse.itemgroupworkflowapi.validator;

public enum Status {

    PENDING("pending"),
    PROCESSING("processing"),
    SATISFIED("satisfied"),
    CANCELLED("cancelled"),
    FAILED("failed");

    private final String name;

    Status(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
