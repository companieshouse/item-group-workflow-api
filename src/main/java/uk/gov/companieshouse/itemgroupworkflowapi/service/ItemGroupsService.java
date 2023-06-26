package uk.gov.companieshouse.itemgroupworkflowapi.service;


import static org.apache.commons.lang.StringUtils.isBlank;

import com.mongodb.MongoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.itemgroupworkflowapi.exception.MongoOperationException;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.itemgroupworkflowapi.exception.ItemNotFoundException;
import uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtils;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Item;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroup;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroupData;
import uk.gov.companieshouse.itemgroupworkflowapi.repository.ItemGroupsRepository;
import uk.gov.companieshouse.logging.util.DataMap;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

@Service
public class ItemGroupsService {
    private static String ITEM_GROUP_CREATE_ID_PREFIX = "IG-";
    public static String MONGO_EXISTS_EXCEPTION_MESSAGE = "Mongo EXISTS operation failed for item group order number : ";
    public static String MONGO_SAVE_EXCEPTION_MESSAGE = "Mongo SAVE operation failed for item group order number : ";
    private final LoggingUtils logger;
    private final ItemGroupsRepository itemGroupsRepository;
    private final LinksGeneratorService linksGenerator;

    public ItemGroupsService(LoggingUtils logger,
                             ItemGroupsRepository itemGroupsRepository,
                             LinksGeneratorService linksGenerator) {
        this.logger = logger;
        this.itemGroupsRepository = itemGroupsRepository;

        if (isBlank(pathToSelf))
            throw new IllegalArgumentException("Path to self URI not configured!");
        this.pathToSelf = pathToSelf;
        this.linksGenerator = linksGenerator;
    }

    public boolean doesItemGroupExist(ItemGroupData itemGroupData){
        boolean itemExists;
        try {
            itemExists = itemGroupsRepository.existsItemGroupByDataOrderNumber(itemGroupData.getOrderNumber());
        } catch (MongoException mex) {
            throw new MongoOperationException(MONGO_EXISTS_EXCEPTION_MESSAGE + itemGroupData.getOrderNumber(), mex);
        }

        return itemExists;
    }

    public ItemGroupData createItemGroup(ItemGroupData itemGroupData) {
        final ItemGroup itemGroup = new ItemGroup();

        String itemGroupId = autoGenerateId();
        itemGroup.setId(itemGroupId);

        setCreationTimeStamp(itemGroup);
        linksGenerator.regenerateLinks(itemGroupData, itemGroupId);
        itemGroup.setData(itemGroupData);

        regenerateLinks(itemGroupData, itemGroupId);

        ItemGroup savedItemGroup;
        try {
            savedItemGroup = itemGroupsRepository.save(itemGroup);
        }
        catch(MongoException mex) {
            throw new MongoOperationException(MONGO_SAVE_EXCEPTION_MESSAGE + itemGroupData.getOrderNumber(), mex);
        }

        final ItemGroup savedItemGroup = itemGroupsRepository.save(itemGroup);

        return savedItemGroup.getData();
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

        final var savedItemGroup = itemGroupsRepository.save(itemGroup);

        return getSavedItem(savedItemGroup, itemId);
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
        final String error = "Not able to find item " + itemId + " in group " + itemGroupId + ".";
        logger.getLogger().error(error, getLogMap(itemGroupId, itemId, error));
        return new ItemNotFoundException(error);
    }

    private ItemGroup findGroup(final String itemGroupId, final String itemId) {
        return itemGroupsRepository.findById(itemGroupId)
                .orElseThrow(() -> itemNotFound(itemGroupId, itemId));
    }

    private Item getSavedItem(final ItemGroup savedItemGroup, final String itemId) {
        return savedItemGroup.getData().getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> itemNotFound(savedItemGroup.getId(), itemId));
    }

    private Map<String, Object> getLogMap(final String itemGroupId, final String itemId, final String error) {
        return new DataMap.Builder()
                .itemGroupId(itemGroupId)
                .itemId(itemId)
                .errors(singletonList(error))
                .build()
                .getLogMap();
    }

}