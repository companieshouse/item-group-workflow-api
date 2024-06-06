package uk.gov.companieshouse.itemgroupworkflowapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ItemStatusUpdateDto(@JsonProperty("order_number") String orderNumber,
                                  @JsonProperty("group_item") String groupItem, ItemDto item) {

    public ItemStatusUpdateDto(String orderNumber, String groupItem, ItemDto item) {
        this.orderNumber = orderNumber;
        this.groupItem = groupItem;
        this.item = item;
    }

    @Override
    public String orderNumber() {
        return orderNumber;
    }

    @Override
    public String groupItem() {
        return groupItem;
    }
}
