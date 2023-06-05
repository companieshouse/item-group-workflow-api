package uk.gov.companieshouse.itemgroupworkflowapi.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroupCreate;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroupJsonPayload;
import uk.gov.companieshouse.itemgroupworkflowapi.repository.ItemGroupsRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.toList;

@Service
public class ItemGroupsService {
    private final ItemGroupsRepository itemGroupsRepository;

    public ItemGroupsService(ItemGroupsRepository itemGroupsRepository) {
        this.itemGroupsRepository = itemGroupsRepository;
    }

    public boolean doesItemGroupExist(ItemGroupJsonPayload itemGroupJsonPayload){
        return false;
//        return itemGroupsRepository.existsItemGroupByDataCompanyNumber(itemGroupJsonPayload.getCompanyNumber());
    }

    public ItemGroupCreate createItemGroup(ItemGroupJsonPayload itemGroupJsonPayload) {
        final ItemGroupCreate itemGroupCreate = new ItemGroupCreate();
        setCreationTimeStamp(itemGroupCreate);
        itemGroupCreate.setData(itemGroupJsonPayload);

        final ItemGroupCreate savedItemGroupCreate = itemGroupsRepository.save(itemGroupCreate);
        return savedItemGroupCreate;
    }

    // TODO DCAC-78 Typed item, error handling
    public Map<String, Object> getItem(final String itemGroupId, final String itemId) {
        final Optional<ItemGroupCreate> itemGroup = itemGroupsRepository.findById(itemGroupId);
        return (Map<String, Object>)
                itemGroup.flatMap(group -> group.getData()
                                                .getItems()
                                                .stream()
                                                .filter(item -> ((Map) item).get("id").equals(itemId))
                                                .findFirst())
                                                .get();
    }

    // TODO DCAC-78 Typed item, error handling
    public Map<String, Object> updateItem(final String itemGroupId,
                                          final String itemId,
                                          final Map<String, Object> updatedItem) {
        final ItemGroupCreate itemGroup = itemGroupsRepository.findById(itemGroupId).get();

        final List<Map<String, Object>> items = itemGroup.getData().getItems();
        final List<Map<String, Object>> updatedItems = items.stream()
                .map(item -> item.get("id").equals(itemId) ? updatedItem : item)
                .collect(toList());
        itemGroup.setUpdatedAt(now());
        itemGroup.getData().setItems((ArrayList) updatedItems);

        itemGroupsRepository.save(itemGroup);

        return updatedItem;
    }

    private void setCreationTimeStamp(final ItemGroupCreate itemGroupCreate) {
        final LocalDateTime now = now();
        itemGroupCreate.setCreatedAt(now);
        itemGroupCreate.setUpdatedAt(now);
    }
}