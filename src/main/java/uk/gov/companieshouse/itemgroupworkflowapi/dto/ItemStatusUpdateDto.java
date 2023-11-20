package uk.gov.companieshouse.itemgroupworkflowapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ItemStatusUpdateDto {

    @JsonProperty("order_number")
    private final String orderNumber;

    @JsonProperty("group_item")
    private final String groupItem;

    private final ItemDto item;

    public ItemStatusUpdateDto(String orderNumber, String groupItem, ItemDto item) {
        this.orderNumber = orderNumber;
        this.groupItem = groupItem;
        this.item = item;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public String getGroupItem() {
        return groupItem;
    }

    public ItemDto getItem() {
        return item;
    }
}
