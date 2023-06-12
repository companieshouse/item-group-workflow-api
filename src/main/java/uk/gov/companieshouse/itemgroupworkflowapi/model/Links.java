package uk.gov.companieshouse.itemgroupworkflowapi.model;

import com.google.gson.Gson;

public class Links {

    private String order;

    private String self;

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
