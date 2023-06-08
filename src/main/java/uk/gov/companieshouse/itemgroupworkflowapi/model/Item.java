package uk.gov.companieshouse.itemgroupworkflowapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.itemgroupworkflowapi.validator.Status;
import uk.gov.companieshouse.itemgroupworkflowapi.validator.Uri;
import uk.gov.companieshouse.itemgroupworkflowapi.validator.ValueOfEnum;

import javax.validation.constraints.NotNull;
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

    @JsonProperty("digital_document_location")
    @Uri // TODO DCAC-78 Rationalise validation
    private String digitalDocumentLocation;

    @JsonProperty("status")
    @NotNull // TODO DCAC-78 Rationalise validation
    @ValueOfEnum(enumClass = Status.class)
    private String status;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getCompanyReference() {
        return companyReference;
    }

    public void setCompanyReference(String companyReference) {
        this.companyReference = companyReference;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescriptionIdentifier() {
        return descriptionIdentifier;
    }

    public void setDescriptionIdentifier(String descriptionIdentifier) {
        this.descriptionIdentifier = descriptionIdentifier;
    }

    public Map getDescriptionValues() {
        return descriptionValues;
    }

    public void setDescriptionValues(Map descriptionValues) {
        this.descriptionValues = descriptionValues;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<ItemCosts> getItemCosts() {
        return itemCosts;
    }

    public void setItemCosts(List<ItemCosts> itemCosts) {
        this.itemCosts = itemCosts;
    }

    public Map getItemOptions() {
        return itemOptions;
    }

    public void setItemOptions(Map itemOptions) {
        this.itemOptions = itemOptions;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }

    public String getPostageCost() {
        return postageCost;
    }

    public void setPostageCost(String postageCost) {
        this.postageCost = postageCost;
    }

    public Boolean getPostalDelivery() {
        return postalDelivery;
    }

    public void setPostalDelivery(Boolean postalDelivery) {
        this.postalDelivery = postalDelivery;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getTotalItemCost() {
        return totalItemCost;
    }

    public void setTotalItemCost(String totalItemCost) {
        this.totalItemCost = totalItemCost;
    }

    public String getDigitalDocumentLocation() {
        return digitalDocumentLocation;
    }

    public void setDigitalDocumentLocation(String digitalDocumentLocation) {
        this.digitalDocumentLocation = digitalDocumentLocation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
