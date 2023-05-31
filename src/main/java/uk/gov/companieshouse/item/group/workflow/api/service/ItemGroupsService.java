package uk.gov.companieshouse.item.group.workflow.api.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.item.group.workflow.api.model.ItemGroup;
import uk.gov.companieshouse.item.group.workflow.api.model.ItemGroupData;
import uk.gov.companieshouse.item.group.workflow.api.model.ItemGroupJSON;
import uk.gov.companieshouse.item.group.workflow.api.repository.ItemGroupsRepository;

import java.time.LocalDateTime;

@Service
public class ItemGroupsService {
    private final ItemGroupsRepository itemGroupsRepository;

    public ItemGroupsService(ItemGroupsRepository itemGroupsRepository) {
        this.itemGroupsRepository = itemGroupsRepository;
    }

    public ItemGroup CreateItemGroup(ItemGroupJSON itemGroupJSON) {
        final ItemGroup itemGroup = new ItemGroup();
        setCreationTimeStamp(itemGroup);
        setItemGroupData(itemGroup, itemGroupJSON);

        final ItemGroup savedItemGroup = itemGroupsRepository.save(itemGroup);
        return savedItemGroup;
    }

    private void setCreationTimeStamp(final ItemGroup itemGroup) {
        final LocalDateTime now = LocalDateTime.now();
        itemGroup.setCreatedAt(now);
        itemGroup.setUpdatedAt(now);
    }

    private void setItemGroupData(ItemGroup itemGroup, ItemGroupJSON itemGroupJSON){
        ItemGroupData itemGroupData = new ItemGroupData();

        itemGroupData.setCompanyNumber(itemGroupJSON.getCompanyNumber());
        itemGroupData.setCompanyName(itemGroupJSON.getCompanyName());

        itemGroup.setData(itemGroupData);
    }
}