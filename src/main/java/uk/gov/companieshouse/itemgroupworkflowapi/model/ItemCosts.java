package uk.gov.companieshouse.itemgroupworkflowapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;

public class ItemCosts {

    @JsonProperty("calculated_cost")
    private String calculatedCost;

    @JsonProperty("discount_applied")
    private String discountApplied;

    @JsonProperty("item_cost")
    private String itemCost;

    @JsonProperty("product_type")
    private String productType;

    public String getCalculatedCost() {
        return calculatedCost;
    }

    public void setCalculatedCost(String calculatedCost) {
        this.calculatedCost = calculatedCost;
    }

    public String getDiscountApplied() {
        return discountApplied;
    }

    public void setDiscountApplied(String discountApplied) {
        this.discountApplied = discountApplied;
    }

    public String getItemCost() {
        return itemCost;
    }

    public void setItemCost(String itemCost) {
        this.itemCost = itemCost;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

}
