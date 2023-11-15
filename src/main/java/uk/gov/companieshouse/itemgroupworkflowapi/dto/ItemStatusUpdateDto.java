package uk.gov.companieshouse.itemgroupworkflowapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ItemStatusUpdateDto {

    @JsonProperty("order_number")
    private String orderNumber;

    @JsonProperty("group_item")
    private String groupItem;

    private ItemDto item;

    public ItemStatusUpdateDto(String orderNumber, String groupItem, ItemDto item) {
        this.orderNumber = orderNumber;
        this.groupItem = groupItem;
        this.item = item;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getGroupItem() {
        return groupItem;
    }

    public void setGroupItem(String groupItem) {
        this.groupItem = groupItem;
    }

    public ItemDto getItem() {
        return item;
    }

    public void setItem(ItemDto item) {
        this.item = item;
    }
}
