package uk.gov.companieshouse.itemgroupworkflowapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ItemCosts {

    @JsonProperty("calculated_cost")
    private String calculatedCost;

    @JsonProperty("discount_applied")
    private String discountApplied;

    @JsonProperty("item_cost")
    private String itemCost;

    @JsonProperty("product_type")
    private String productType;

    @Override
    public String toString() {
        return "item_costs {" +
                "calculated_cost='" + calculatedCost + '\'' +
                ", discount_applied='" + discountApplied +
                ", item_cost='" + itemCost +
                ", product_type='" + productType +
                '}';
    }

}
