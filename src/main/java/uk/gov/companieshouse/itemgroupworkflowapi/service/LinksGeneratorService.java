package uk.gov.companieshouse.itemgroupworkflowapi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Links;

import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * Service that generates the links for the item ID specified.
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
     * Generates the links for the item group identified.
     * @param orderPath the partial orderPath URI from which the item group is created
     * @param itemGroupId the ID for the item group
     * @return the appropriate {@link Links}
     */
    public Links generateItemGroupLinks(final String orderPath, final String itemGroupId) {
        if (isBlank(itemGroupId)) {
            throw new IllegalArgumentException("Item Group ID not populated!");
        }
        final Links links = new Links();
        links.setOrder(orderPath);
        links.setSelf(pathToSelf + "/" + itemGroupId);
        return links;
    }

}
