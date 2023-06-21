package uk.gov.companieshouse.itemgroupworkflowapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Links {

    @JsonProperty("order")
    private String order;

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    private String self;
    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    @Override
    public String toString() {
        return "links {" +
            "order='" + order + '\'' +
            "self='" + self + '\'' +
                '}';
    }
}
