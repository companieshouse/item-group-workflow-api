package uk.gov.companieshouse.itemgroupworkflowapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.mongodb.core.mapping.Field;

public class ItemCosts {

    @JsonProperty("calculated_cost")
    @Field("calculated_cost")
    private String calculatedCost;

    @JsonProperty("discount_applied")
    @Field("discount_applied")
    private String discountApplied;

    @JsonProperty("item_cost")
    @Field("item_cost")
    private String itemCost;

    @JsonProperty("product_type")
    @Field("product_type")
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
