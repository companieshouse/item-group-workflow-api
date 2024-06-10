package uk.gov.companieshouse.itemgroupworkflowapi.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.gson.GsonBuilder;
import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.itemgroupworkflowapi.adapter.LocalDateTimeTypeAdapter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.companieshouse.itemgroupworkflowapi.util.Constants.JSON_DATE_TIME_FORMAT;

public class Item {

    private String companyName;

    private String companyNumber;

    private String customerReference;

    private String description;

    private String descriptionIdentifier;

    private Map<String, String> descriptionValues;

    private String etag;

    @Field("id")
    private String id;

    private List<ItemCosts> itemCosts;

    private Map<String,Object> itemOptions;

    private String kind;

    private ItemLinks links;

    private String postageCost;

    private Boolean postalDelivery;

    private Integer quantity;

    private String totalItemCost;

    private String digitalDocumentLocation;

    private String status;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern=JSON_DATE_TIME_FORMAT)
    private LocalDateTime updatedAt;

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

    public String getCustomerReference() {
        return customerReference;
    }

    public void setCustomerReference(String customerReference) {
        this.customerReference = customerReference;
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

    public Map<String, String> getDescriptionValues() {
        return descriptionValues;
    }

    public void setDescriptionValues(Map<String, String> descriptionValues) {
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

    public Map<String, Object> getItemOptions() {
        return itemOptions;
    }

    public void setItemOptions(Map<String, Object> itemOptions) {
        this.itemOptions = itemOptions;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public ItemLinks getLinks() {
        return links;
    }

    public void setLinks(ItemLinks links) {
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter()).create()
                .toJson(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var item = (Item) o;
        return Objects.equals(companyName, item.companyName) &&
                Objects.equals(companyNumber, item.companyNumber) &&
                Objects.equals(customerReference, item.customerReference) &&
                Objects.equals(description, item.description) &&
                Objects.equals(descriptionIdentifier, item.descriptionIdentifier) &&
                Objects.equals(descriptionValues, item.descriptionValues) &&
                Objects.equals(etag, item.etag) && Objects.equals(id, item.id) &&
                Objects.equals(itemCosts, item.itemCosts) &&
                Objects.equals(itemOptions, item.itemOptions) &&
                Objects.equals(kind, item.kind) &&
                Objects.equals(links, item.links) &&
                Objects.equals(postageCost, item.postageCost) &&
                Objects.equals(postalDelivery, item.postalDelivery) &&
                Objects.equals(quantity, item.quantity) &&
                Objects.equals(totalItemCost, item.totalItemCost) &&
                Objects.equals(digitalDocumentLocation, item.digitalDocumentLocation) &&
                Objects.equals(status, item.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(companyName,
                companyNumber,
                customerReference,
                description,
                descriptionIdentifier,
                descriptionValues,
                etag,
                id,
                itemCosts,
                itemOptions,
                kind,
                links,
                postageCost,
                postalDelivery,
                quantity,
                totalItemCost,
                digitalDocumentLocation,
                status);
    }
}
