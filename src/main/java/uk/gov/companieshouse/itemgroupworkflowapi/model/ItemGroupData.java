package uk.gov.companieshouse.itemgroupworkflowapi.model;

import com.google.gson.Gson;

import java.time.LocalDateTime;
import java.util.List;

public class ItemGroupData {

    private DeliveryDetails deliveryDetails;
    public DeliveryDetails getDeliveryDetails() {return deliveryDetails;}

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

    private Links links;
    public Links getLinks() {return links;}

    private String orderNumber;
    public String getOrderNumber() {
        return orderNumber;
    }

    private LocalDateTime orderedAt;
    public LocalDateTime getOrderedAt() {
        return orderedAt;
    }

    private OrderedBy orderedBy;
    public OrderedBy getOrderedBy() {
        return orderedBy;
    }

    private String paymentReference;
    public String getPaymentReference() {
        return paymentReference;
    }

    private String reference;
    public String getReference() {
        return reference;
    }

    private String totalOrderCost;
    public String getTotalOrderCost() {
        return totalOrderCost;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}