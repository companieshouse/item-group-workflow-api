package uk.gov.companieshouse.itemgroupworkflowapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;
import java.util.Map;

public class Item {

    @JsonProperty("company_name")
    private String companyName;

    @JsonProperty("company_number")
    private String companyNumber;

    @JsonProperty("company_reference")
    private String companyReference;

    @JsonProperty("description")
    private String description;

    @JsonProperty("description_identifier")
    @Field("description_identifier")
    private String descriptionIdentifier;

    @JsonProperty("description_values")
    @Field("description_values")
    private Map descriptionValues;

    @JsonProperty("etag")
    private String etag;

    @JsonProperty("id")
    private String id;

    @JsonProperty("item_costs")
    @Field("item_costs")
    private List<ItemCosts> itemCosts;

    @JsonProperty("item_options")
    @Field("item_options")
    private Map itemOptions;

    @JsonProperty("kind")
    private String kind;

    @JsonProperty("links")
    private Links links;

    @JsonProperty("postage_cost")
    @Field("postage_cost")
    private String postageCost;

    @JsonProperty("postal_delivery")
    private Boolean postalDelivery;

    @JsonProperty("quantity")
    private Integer quantity;

    @JsonProperty("total_item_cost")
    @Field("total_item_cost")
    private String totalItemCost;

    @Override
    public String toString() {
        return "item {" +
                "company_name='" + companyName + '\'' +
                ", company_number='" + companyNumber +
                ", company_reference='" + companyReference +
                ", description='" + description +
                ", description_identifier='" + descriptionIdentifier +
                ", description_values='" + descriptionValues +
                ", etag='" + etag +
                ", id='" + id +
                ", item_costs='" + itemCosts +
                ", item_options='" + itemOptions +
                ", kind='" + kind +
                ", links='" + links +
                ", postage_cost='" + postageCost +
                ", postal_delivery='" + postalDelivery +
                ", quantity='" + quantity +
                ", total_item_cost='" + totalItemCost +
                '}';
    }

}
