package uk.gov.companieshouse.itemgroupworkflowapi.util;

import java.util.List;
import java.util.stream.Collectors;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Item;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroupData;

public class ItemGroupDataUtils {

    private ItemGroupDataUtils() {}

    public static List<String> getItemIds(final ItemGroupData itemGroup) {
        return itemGroup.getItems().stream()
            .map(Item::getId)
            .collect(Collectors.toList());
    }

}
