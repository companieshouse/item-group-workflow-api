package uk.gov.companieshouse.itemgroupworkflowapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;


import java.time.LocalDateTime;
import java.util.ArrayList;

public class ItemGroupJsonPayload {

    @JsonProperty("delivery_details")
    private DeliveryDetails deliveryDetails;
    public DeliveryDetails getDeliveryDetails() {return deliveryDetails;}

    @JsonProperty("items")
    private ArrayList items;
    public ArrayList getItems() {return items;}

    public void setItems(ArrayList items) {
        this.items = items;
    }

    @JsonProperty("links")
    private Links links;
    public Links getLinks() {return links;}

    @JsonProperty("order_number")
    private String orderNumber;
    public String getOrderNumber() {
        return orderNumber;
    }

    @JsonProperty("ordered_at")
    private LocalDateTime orderedAt;
    public LocalDateTime getOrderedAt() {
        return orderedAt;
    }

    @JsonProperty("ordered_by")
    private OrderedBy orderedBy;
    public OrderedBy getOrderedBy() {
        return orderedBy;
    }

    @JsonProperty("payment_reference")
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
    private String totalOrderCost;
    public String getTotalOrderCost() {
        return totalOrderCost;
    }

    @Override
    public String toString() {
        return "ItemGroupJsonPayload {" +
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