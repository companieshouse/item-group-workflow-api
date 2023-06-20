package uk.gov.companieshouse.itemgroupworkflowapi.model;

import com.google.gson.Gson;

public class ItemLinks {

    private String originalItem;

    private String self;

    public String getOriginalItem() {
        return originalItem;
    }

    public void setOriginalItem(String originalItem) {
        this.originalItem = originalItem;
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
