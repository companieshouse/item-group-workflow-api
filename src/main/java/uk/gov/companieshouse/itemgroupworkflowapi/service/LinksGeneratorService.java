package uk.gov.companieshouse.itemgroupworkflowapi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroupData;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemLinks;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Links;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Service that generates the links for the item group/item identified.
 */
@Service
public class LinksGeneratorService {

    private final String pathToSelf;

    /**
     * Constructor.
     * @param pathToSelf configured path to self URI
     */
    public LinksGeneratorService(
            final @Value("${uk.gov.companieshouse.itemgroupworkflowapi.createitemgroup}") String pathToSelf) {
        if (isBlank(pathToSelf)) {
            throw new IllegalArgumentException("Path to self URI not configured!");
        }
        this.pathToSelf = pathToSelf;
    }

    /**
     * Regenerates the links for both the item group itself and each of the items in the group.
     * @param itemGroupData the item group data
     * @param itemGroupId the ID of the item group
     */
    public void regenerateLinks(final ItemGroupData itemGroupData, final String itemGroupId) {
        itemGroupData.setLinks(generateItemGroupLinks(itemGroupData.getLinks().getOrder(), itemGroupId));
        itemGroupData.getItems().forEach(item ->
                item.setLinks(generateItemLinks(item.getLinks().getOriginalItem(), itemGroupId, item.getId()))
        );
    }

    /**
     * Generates the links for the item group identified.
     * @param orderPath the partial orderPath URI from which the item group is created
     * @param itemGroupId the ID for the item group
     * @return the appropriate {@link Links}
     */
    Links generateItemGroupLinks(final String orderPath, final String itemGroupId) {
        if (isBlank(itemGroupId)) {
            throw new IllegalArgumentException("Item Group ID not populated!");
        }
        final var links = new Links();
        links.setOrder(orderPath);
        links.setSelf(pathToSelf + "/" + itemGroupId);
        return links;
    }

    /**
     * Generates the links for the item identified.
     * @param originalItem the original partial URI for the item
     * @param itemId the ID for the item
     * @return the appropriate {@link ItemLinks}
     */
    ItemLinks generateItemLinks(final String originalItem,
                                final String itemGroupId,
                                final String itemId) {
        if (isBlank(itemGroupId)) {
            throw new IllegalArgumentException("Item Group ID not populated!");
        }
        if (isBlank(itemId)) {
            throw new IllegalArgumentException("Item ID not populated!");
        }
        final var links = new ItemLinks();
        links.setOriginalItem(originalItem);
        links.setSelf(pathToSelf + "/" + itemGroupId + "/items/" +itemId);
        return links;
    }

}
