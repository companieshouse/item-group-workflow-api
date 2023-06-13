package uk.gov.companieshouse.itemgroupworkflowapi.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.itemgroupworkflowapi.exception.ItemNotFoundException;
import uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtils;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Item;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroup;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroupData;
import uk.gov.companieshouse.itemgroupworkflowapi.repository.ItemGroupsRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Calendar;

import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.toList;

@Service
public class ItemGroupsService {
    private static String ITEM_GROUP_CREATE_ID_PREFIX = "IG-";
    private final LoggingUtils logger;
    private final ItemGroupsRepository itemGroupsRepository;
    private final LinksGeneratorService linksGenerator;

    public ItemGroupsService(LoggingUtils logger,
                             ItemGroupsRepository itemGroupsRepository,
                             LinksGeneratorService linksGenerator) {
        this.logger = logger;
        this.itemGroupsRepository = itemGroupsRepository;
        this.linksGenerator = linksGenerator;
    }

    public boolean doesItemGroupExist(ItemGroupData itemGroupData){
        return itemGroupsRepository.existsItemGroupByDataOrderNumber(itemGroupData.getOrderNumber());
    }

    public ItemGroup createItemGroup(ItemGroupData itemGroupData) {
        final ItemGroup itemGroup = new ItemGroup();

        String itemGroupId = autoGenerateId();
        itemGroup.setId(itemGroupId);

        setCreationTimeStamp(itemGroup);
        generateLinks(itemGroupData, itemGroupId);
        itemGroup.setData(itemGroupData);

        final ItemGroup savedItemGroup = itemGroupsRepository.save(itemGroup);
        return savedItemGroup;
    }

    public Item getItem(final String itemGroupId, final String itemId) {
        final var itemGroup = findGroup(itemGroupId, itemId);
        return itemGroup.getData()
                        .getItems()
                        .stream()
                        .filter(item -> item.getId().equals(itemId))
                        .findFirst()
                        .orElseThrow(() -> itemNotFound(itemGroupId, itemId));
    }

    public Item updateItem(final String itemGroupId,
                           final String itemId,
                           final Item updatedItem) {
        final var itemGroup = findGroup(itemGroupId, itemId);

        final var now = now();
        updatedItem.setUpdatedAt(now);
        final var data = itemGroup.getData();
        final var items = data.getItems();
        final var updatedItems = items.stream()
                                      .map(item -> item.getId().equals(itemId) ? updatedItem : item)
                                      .collect(toList());
        itemGroup.setUpdatedAt(now);
        data.setItems(updatedItems);

        itemGroupsRepository.save(itemGroup);

        return updatedItem;
    }


    private void setCreationTimeStamp(final ItemGroup itemGroup) {
        final LocalDateTime now = LocalDateTime.now();
        itemGroup.setCreatedAt(now);
        itemGroup.setUpdatedAt(now);
    }

    private String autoGenerateId() {
        SecureRandom random = new SecureRandom();
        byte[] values = new byte[4];
        random.nextBytes(values);
        String rand = String.format("%04d", random.nextInt(9999));
        String time = String.format("%08d", Calendar.getInstance().getTimeInMillis() / 100000L);
        String rawId = rand + time;
        String[] tranId = rawId.split("(?<=\\G.{6})");
        return ITEM_GROUP_CREATE_ID_PREFIX + String.join("-", tranId);
    }

    private ItemNotFoundException itemNotFound(final String itemGroupId, final String itemId) {
        // TODO DCAC-78 Structured logging
        final String error = "Not able to find item " + itemId + " in group " + itemGroupId + ".";
        logger.getLogger().error(error);
        return new ItemNotFoundException(error);
    }

    private ItemGroup findGroup(final String itemGroupId, final String itemId) {
        return itemGroupsRepository.findById(itemGroupId)
                .orElseThrow(() -> itemNotFound(itemGroupId, itemId));
    }

    private void generateLinks(final ItemGroupData itemGroupData, final String itemGroupId) {
        itemGroupData.setLinks(linksGenerator.generateItemGroupLinks(itemGroupData.getLinks().getOrder(), itemGroupId));
        itemGroupData.getItems().stream().forEach(item ->
            item.setLinks(
                    linksGenerator.generateItemLinks(item.getLinks().getOriginalItem(), itemGroupId, item.getId()))
        );
    }
}