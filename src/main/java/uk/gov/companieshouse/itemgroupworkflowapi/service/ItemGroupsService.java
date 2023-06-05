package uk.gov.companieshouse.itemgroupworkflowapi.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroup;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroupData;
import uk.gov.companieshouse.itemgroupworkflowapi.repository.ItemGroupsRepository;

import java.time.LocalDateTime;

@Service
public class ItemGroupsService {
    private final ItemGroupsRepository itemGroupsRepository;

    public ItemGroupsService(ItemGroupsRepository itemGroupsRepository) {
        this.itemGroupsRepository = itemGroupsRepository;
    }

    public boolean doesItemGroupExist(ItemGroupData itemGroupData){
        return itemGroupsRepository.existsItemGroupByDataOrderNumber(itemGroupData.getOrderNumber());
    }

    public ItemGroup createItemGroup(ItemGroupData itemGroupData) {
        final ItemGroup itemGroup = new ItemGroup();
        setCreationTimeStamp(itemGroup);
        itemGroup.setData(itemGroupData);

        final ItemGroup savedItemGroup = itemGroupsRepository.save(itemGroup);
        return savedItemGroup;
    }

    private void setCreationTimeStamp(final ItemGroup itemGroup) {
        final LocalDateTime now = LocalDateTime.now();
        itemGroup.setCreatedAt(now);
        itemGroup.setUpdatedAt(now);
    }
}