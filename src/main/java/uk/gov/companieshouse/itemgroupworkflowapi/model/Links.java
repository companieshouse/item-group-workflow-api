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

    @Override
    public String toString() {
        return "links {" +
                "address_line_1='" + order + '\'' +
                '}';
    }
}
