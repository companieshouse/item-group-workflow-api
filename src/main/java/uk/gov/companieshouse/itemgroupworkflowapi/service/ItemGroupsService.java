package uk.gov.companieshouse.itemgroupworkflowapi.service;

import static org.apache.commons.lang.StringUtils.isBlank;

import com.mongodb.MongoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.itemgroupworkflowapi.exception.MongoOperationException;
import uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtils;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroup;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroupData;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemLinks;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Links;
import uk.gov.companieshouse.itemgroupworkflowapi.repository.ItemGroupsRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Calendar;

@Service
public class ItemGroupsService {
    private static String ITEM_GROUP_CREATE_ID_PREFIX = "IG-";
    public static String MONGO_EXISTS_EXCEPTION_MESSAGE = "Mongo EXISTS operation failed for item group order number : ";
    public static String MONGO_SAVE_EXCEPTION_MESSAGE = "Mongo SAVE operation failed for item group order number : ";
    private final LoggingUtils logger;
    private final ItemGroupsRepository itemGroupsRepository;
    private final String pathToSelf;

    public ItemGroupsService(
        LoggingUtils logger,
        ItemGroupsRepository itemGroupsRepository,
        final @Value("${uk.gov.companieshouse.itemgroupworkflowapi.createitemgroup}") String pathToSelf) {
        this.logger = logger;
        this.itemGroupsRepository = itemGroupsRepository;

        if (isBlank(pathToSelf))
            throw new IllegalArgumentException("Path to self URI not configured!");
        this.pathToSelf = pathToSelf;
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
        itemGroup.setData(itemGroupData);

        regenerateLinks(itemGroupData, itemGroupId);

        ItemGroup savedItemGroup;
        try {
            savedItemGroup = itemGroupsRepository.save(itemGroup);
        }
        catch(MongoException mex) {
            throw new MongoOperationException(MONGO_SAVE_EXCEPTION_MESSAGE + itemGroupData.getOrderNumber(), mex);
        }
        return savedItemGroup.getData();
    }

    private void setCreationTimeStamp(final ItemGroup itemGroup) {
        final LocalDateTime now = LocalDateTime.now();
        itemGroup.setCreatedAt(now);
        itemGroup.setUpdatedAt(now);
    }

    public void regenerateLinks(final ItemGroupData itemGroupData, final String itemGroupId) {

        itemGroupData.setLinks(generateItemGroupLinks(itemGroupData.getLinks().getOrder(), itemGroupId));

        itemGroupData.getItems().forEach(item ->
            item.setItemLinks(generateItemLinks(item.getItemLinks().getOriginalItem(), itemGroupId, item.getId()))
        );
    }

    Links generateItemGroupLinks(final String orderPath, final String itemGroupId) {
        if (isBlank(itemGroupId)) {
            throw new IllegalArgumentException("Item Group ID not populated!");
        }
        final Links links = new Links();
        links.setOrder(orderPath);
        links.setSelf(pathToSelf + "/" + itemGroupId);
        return links;
    }

    ItemLinks generateItemLinks(final String originalItem,
                                final String itemGroupId,
                                final String itemId) {
        if (isBlank(itemGroupId)) {
            throw new IllegalArgumentException("Item Group ID not populated!");
        }
        if (isBlank(itemId)) {
            throw new IllegalArgumentException("Item ID not populated!");
        }
        final ItemLinks links = new ItemLinks();
        links.setOriginalItem(originalItem);
        links.setSelf(pathToSelf + "/" + itemGroupId + "/items/" +itemId);
        return links;
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
}