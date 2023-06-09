package uk.gov.companieshouse.itemgroupworkflowapi.model;

import com.google.gson.Gson;

public class Links {

    private String order;

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
