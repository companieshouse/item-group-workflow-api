package uk.gov.companieshouse.itemgroupworkflowapi.service;

import uk.gov.companieshouse.itemgroupworkflowapi.model.Item;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroupData;

public interface MessageSender {
    void sendMessage(ItemGroupData group, Item item);
}
