package uk.gov.companieshouse.itemgroupworkflowapi.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtils;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroup;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroupData;
import uk.gov.companieshouse.itemgroupworkflowapi.repository.ItemGroupsRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Calendar;

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