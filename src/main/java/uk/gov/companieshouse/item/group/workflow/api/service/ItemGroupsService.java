package uk.gov.companieshouse.item.group.workflow.api.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.item.group.workflow.api.model.ItemGroupCreate;
import uk.gov.companieshouse.item.group.workflow.api.model.ItemGroupJsonPayload;
import uk.gov.companieshouse.item.group.workflow.api.repository.ItemGroupsRepository;

import java.time.LocalDateTime;

@Service
public class ItemGroupsService {
    private final ItemGroupsRepository itemGroupsRepository;

    public ItemGroupsService(ItemGroupsRepository itemGroupsRepository) {
        this.itemGroupsRepository = itemGroupsRepository;
    }

    public boolean doesCompanyExist(ItemGroupJsonPayload itemGroupJsonPayload){
        return itemGroupsRepository.doesCompanyNumberExist(itemGroupJsonPayload.getCompanyNumber());
    }

    public ItemGroupCreate CreateItemGroup(ItemGroupJsonPayload itemGroupJsonPayload) {
        final ItemGroupCreate itemGroupCreate = new ItemGroupCreate();
        setCreationTimeStamp(itemGroupCreate);
        itemGroupCreate.setData(itemGroupJsonPayload);

        final ItemGroupCreate savedItemGroupCreate = itemGroupsRepository.save(itemGroupCreate);
        return savedItemGroupCreate;
    }

    private void setCreationTimeStamp(final ItemGroupCreate itemGroupCreate) {
        final LocalDateTime now = LocalDateTime.now();
        itemGroupCreate.setCreatedAt(now);
        itemGroupCreate.setUpdatedAt(now);
    }
}