package uk.gov.companieshouse.itemgroupworkflowapi.validation;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.itemgroupworkflowapi.model.DeliveryDetails;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Item;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemCostProductType;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemCosts;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemDescriptionIdentifier;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroupData;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemKind;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Links;

import java.util.ArrayList;
import java.util.List;

@Component
public class ItemGroupsValidator {
    public static final String ORDER_NUMBER_INVALID = "Items Group : order number invalid";
    public static final String ITEM_GROUP_ITEMS_MISSING = "Items Group : items not provided";
    public static final String ITEM_GROUP_LINKS_MISSING = "Items Group : links not provided";
    public static final String LINKS_ORDER_NUMBER_MISSING = "Items Group : link order number not provided";
    public static final String INVALID_DESCRIPTION_IDENTIFIER = "Items Group : invalid item description identifier : ";
    public static final String INVALID_ITEM_COST_PRODUCT_TYPE = "Items Group : invalid item cost product type : ";
    public static final String INVALID_ITEM_KIND_NAME = "Items Group : invalid item kind : ";
    public static final String COMPANY_NUMBER_MISSING = "Items Group Delivery Details : missing item company number for : ";
    public static final String COMPANY_NAME_MISSING = "Items Group Delivery Details : missing item company name for : ";

    public List<String> validateCreateItemData(ItemGroupData dto) {
        final List<String> errors = new ArrayList<>();

        if (orderNumberValid(dto, errors) &&
            itemsPresent(dto, errors) &&
            linksPresentWithOrderNumber(dto, errors))
        {
            validateItemDescriptionIdentifier(dto, errors);
            validateItemKind(dto, errors);
            validateItemCostsProductType(dto, errors);

            // TODO finish the test for this last one!!!
            validateCompanyNumberAndName(dto, errors);
        }

        return errors;
    }

    private boolean orderNumberValid(ItemGroupData dto, List<String> errors) {
        if (StringUtils.isBlank(dto.getOrderNumber())) {
            errors.add(ORDER_NUMBER_INVALID);
            return false;
        }

        return true;
    }

    private boolean itemsPresent(ItemGroupData dto, List<String> errors) {
        List<Item> items = dto.getItems();

        if (isNull(items) || items.isEmpty()) {
            errors.add(ITEM_GROUP_ITEMS_MISSING);
            return false;
        }

        return true;
    }

    private boolean linksPresentWithOrderNumber(ItemGroupData dto, List<String> errors) {
        Links links = dto.getLinks();

        if (isNull(links)) {
            errors.add(ITEM_GROUP_LINKS_MISSING);
            return false;
        }

        if (StringUtils.isBlank(links.getOrder())) {
            errors.add(LINKS_ORDER_NUMBER_MISSING);
            return false;
        }

        return true;
    }
    /**
     * Taken from: <a href="https://developer-specs.cidev.aws.chdev.org/item-group-workflow-api/reference/itemgroups/create-item-group">create item group</a>
     * Optional : The company name associated with this item (mandatory field if the item relates to a specific company)
     * Optional : The company number associated with this item (mandatory field if the item relates to a specific company)
     * Both flagged as optional and 'mandatory field if the item relates to a specific company'
     * Working assumption : if delivery_details.company_name is present then both these fields are mandatory.
     */
    private void validateCompanyNumberAndName(ItemGroupData dto, List<String> errors) {
        DeliveryDetails deliveryDetails = dto.getDeliveryDetails();
        if (isNull(deliveryDetails))
            return;

        String deliveryCompanyName = deliveryDetails.getCompanyName();
        if (StringUtils.isBlank(deliveryCompanyName))
            return;

        for (Item item : dto.getItems()) {
            String companyNumber = item.getCompanyNumber();
            String companyName = item.getCompanyName();

            if (StringUtils.isBlank(companyNumber)) {
                errors.add(COMPANY_NUMBER_MISSING + deliveryCompanyName);
            }

            if (StringUtils.isBlank(companyName)) {
                errors.add(COMPANY_NAME_MISSING + deliveryCompanyName);
            }
        }
    }

    private void validateItemDescriptionIdentifier(ItemGroupData dto, List<String> errors) {
        for (Item item : dto.getItems()) {
            String descriptionIdentifier = item.getDescriptionIdentifier();

            if (isNull(ItemDescriptionIdentifier.getEnumValue(descriptionIdentifier))) {
                errors.add(INVALID_DESCRIPTION_IDENTIFIER + descriptionIdentifier);
            }
        }
    }

    private void validateItemCostsProductType(ItemGroupData dto, List<String> errors) {
        for (Item item : dto.getItems()) {
            if(nonNull(item.getItemCosts())) {
                for (ItemCosts itemCost : item.getItemCosts()) {
                    String productType = itemCost.getProductType();

                    if (isNull(ItemCostProductType.getEnumValue(productType))) {
                        errors.add(INVALID_ITEM_COST_PRODUCT_TYPE + productType);
                    }
                }
            }
        }
    }

    private void validateItemKind(ItemGroupData dto, List<String> errors) {
        for (Item item : dto.getItems()) {
            String itemKind = item.getKind();

            if (isNull(ItemKind.getEnumValue(itemKind))) {
                errors.add(INVALID_ITEM_KIND_NAME + itemKind);
            }
        }
    }
}