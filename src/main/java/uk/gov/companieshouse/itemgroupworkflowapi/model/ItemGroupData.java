package uk.gov.companieshouse.itemgroupworkflowapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.mongodb.core.mapping.Field;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ItemGroupData {

    @JsonProperty("delivery_details")
    @Field("delivery_details")
    private DeliveryDetails deliveryDetails;
    public DeliveryDetails getDeliveryDetails() {return deliveryDetails;}

    @JsonProperty("items")
    private List<Item> items;
    public List<Item> getItems() {return items;}

    public void setDeliveryDetails(DeliveryDetails deliveryDetails) {
        this.deliveryDetails = deliveryDetails;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public void setLinks(Links links) {
        this.links = links;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public void setOrderedAt(LocalDateTime orderedAt) {
        this.orderedAt = orderedAt;
    }

    public void setOrderedBy(OrderedBy orderedBy) {
        this.orderedBy = orderedBy;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public void setTotalOrderCost(String totalOrderCost) {
        this.totalOrderCost = totalOrderCost;
    }

    @JsonProperty("links")
    private Links links;
    public Links getLinks() { return links; }

    @JsonProperty("order_number")
    @Field("order_number")
    private String orderNumber;
    public String getOrderNumber() {
        return orderNumber;
    }

    @JsonProperty("ordered_at")
    @Field("ordered_at")
    private LocalDateTime orderedAt;
    public LocalDateTime getOrderedAt() {
        return orderedAt;
    }

    @JsonProperty("ordered_by")
    @Field("ordered_by")
    private OrderedBy orderedBy;
    public OrderedBy getOrderedBy() {
        return orderedBy;
    }

    @JsonProperty("payment_reference")
    @Field("payment_reference")
    private String paymentReference;
    public String getPaymentReference() {
        return paymentReference;
    }

    @JsonProperty("reference")
    private String reference;
    public String getReference() {
        return reference;
    }

    @JsonProperty("total_order_cost")
    @Field("total_order_cost")
    private String totalOrderCost;
    public String getTotalOrderCost() {
        return totalOrderCost;
    }

    @Override
    public String toString() {
        return "ItemGroupData {" +
                "delivery_details='" + deliveryDetails +
                ", items='" + items +
                ", links='" + links +
                " order_number='" + orderNumber +
                " ordered_at='" + orderedAt +
                " ordered_by='" + orderedBy +
                " payment_reference='" + paymentReference +
                " reference='" + reference +
                " total_order_cost='" + totalOrderCost +
                '}';
    }
}