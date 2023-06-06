package uk.gov.companieshouse.itemgroupworkflowapi.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtils;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroup;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroupData;
import uk.gov.companieshouse.itemgroupworkflowapi.repository.ItemGroupsRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.toList;

@Service
public class ItemGroupsService {
    private static String ITEM_GROUP_CREATE_ID_PREFIX = "IG-";
    private final LoggingUtils logger;
    private final ItemGroupsRepository itemGroupsRepository;

    public ItemGroupsService(LoggingUtils logger, ItemGroupsRepository itemGroupsRepository) {
        this.logger = logger;
        this.itemGroupsRepository = itemGroupsRepository;
    }

    public boolean doesItemGroupExist(ItemGroupData itemGroupData){
        return itemGroupsRepository.existsItemGroupByDataOrderNumber(itemGroupData.getOrderNumber());
    }

    public ItemGroup createItemGroup(ItemGroupData itemGroupData) {
        final ItemGroup itemGroup = new ItemGroup();

        try {
            String itemGroupId = autoGenerateId();
            itemGroup.setId(itemGroupId);
        } catch (Exception ex) {
            logger.getLogger().error("ID set error : ", ex);
        }

        setCreationTimeStamp(itemGroup);
        itemGroup.setData(itemGroupData);

        final ItemGroup savedItemGroup = itemGroupsRepository.save(itemGroup);
        return savedItemGroup;
    }

    // TODO DCAC-78 Typed item, error handling
    public Map<String, Object> getItem(final String itemGroupId, final String itemId) {
        final Optional<ItemGroup> itemGroup = itemGroupsRepository.findById(itemGroupId);
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
        final ItemGroup itemGroup = itemGroupsRepository.findById(itemGroupId).get();

        final List<Map<String, Object>> items = itemGroup.getData().getItems();
        final List<Map<String, Object>> updatedItems = items.stream()
                .map(item -> item.get("id").equals(itemId) ? updatedItem : item)
                .collect(toList());
        itemGroup.setUpdatedAt(now());
        itemGroup.getData().setItems((ArrayList) updatedItems);

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
}