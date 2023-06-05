package uk.gov.companieshouse.itemgroupworkflowapi.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroupCreate;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroupJsonPayload;
import uk.gov.companieshouse.itemgroupworkflowapi.repository.ItemGroupsRepository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

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

    private void setCreationTimeStamp(final ItemGroupCreate itemGroupCreate) {
        final LocalDateTime now = LocalDateTime.now();
        itemGroupCreate.setCreatedAt(now);
        itemGroupCreate.setUpdatedAt(now);
    }
}