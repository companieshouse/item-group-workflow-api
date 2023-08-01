package uk.gov.companieshouse.itemgroupworkflowapi.validation;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.itemgroupworkflowapi.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isEmpty;

@Component
public class ItemGroupsValidator {

    private static final Map<String,String> MANDATORY_CERTIFIED_COPY_FILING_HISTORY_DOCUMENT_FIELDS =
            Map.of("filing_history_description", "description",
                    "filing_history_type", "type",
                    "filing_history_id", "ID");

    public static final String ORDER_NUMBER_INVALID = "Items Group : order number invalid";
    public static final String ITEM_GROUP_ITEMS_MISSING = "Items Group : items not provided";
    public static final String ITEM_GROUP_LINKS_MISSING = "Items Group : links not provided";
    public static final String ITEM_LINKS_MISSING = "Items : links not provided";
    public static final String LINKS_ORDER_NUMBER_MISSING = "Items Group : link order number not provided";
    public static final String ITEM_LINKS_ORIGINAL_ITEM_MISSING = "Items: Original item missing from Item Links";
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
            validateItemLinks(dto, errors);
            validateItemKind(dto, errors);
            validateItemCostsProductType(dto, errors);
            validateDeliveryDetailsCompanyNumberAndName(dto, errors);
            validateItemOptions(dto, errors);
        }

        return errors;
    }

    private boolean orderNumberValid(ItemGroupData dto, List<String> errors) {
        if (isBlank(dto.getOrderNumber())) {
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

        if (isBlank(links.getOrder())) {
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
    private void validateDeliveryDetailsCompanyNumberAndName(ItemGroupData dto, List<String> errors) {
        DeliveryDetails deliveryDetails = dto.getDeliveryDetails();
        if (isNull(deliveryDetails))
            return;

        String deliveryCompanyName = deliveryDetails.getCompanyName();
        if (isBlank(deliveryCompanyName))
            return;

        for (Item item : dto.getItems()) {
            if (isBlank(item.getCompanyNumber())) {
                errors.add(COMPANY_NUMBER_MISSING + deliveryCompanyName);
            }

            if (isBlank(item.getCompanyName())) {
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

    private void validateItemLinks(ItemGroupData dto, List<String> errors){
        for (Item item : dto.getItems()){
            ItemLinks itemLinks = item.getLinks();

            if(isNull(itemLinks)){
                errors.add(ITEM_LINKS_MISSING);
                return;
            }
            if(isEmpty(itemLinks.getOriginalItem())){
                errors.add(ITEM_LINKS_ORIGINAL_ITEM_MISSING);
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

    private void validateItemOptions(final ItemGroupData itemGroup, final List<String> errors) {
        itemGroup.getItems().forEach(item -> validateItemOptions(item, errors));
    }

    private void validateItemOptions(final Item item, final List<String> errors) {
        final var options = item.getItemOptions();
        if (isNull(options) || options.isEmpty()) {
            errors.add("Missing item options for certified copy item " + item.getId() + ".");
            return;
        }
        final var kind = ItemKind.getEnumValue(item.getKind());
        if (kind == null) {
            return; // This should not happen due to prior validation of kind.
        }
        switch (kind) {
            case ITEM_CERTIFIED_COPY:
                validateCertifiedCopyItemOptions(item, options, errors);
            break;

            default:
                // For now, we only validate item options for certified copies. Nothing to do here.
            break;
        }
    }

    private void validateCertifiedCopyItemOptions(final Item item, final Map<String, Object> options, final List<String> errors) {
        final var filingHistoryDocuments = (List<Object>) options.get("filing_history_documents");
        if (isNull(filingHistoryDocuments) || filingHistoryDocuments.isEmpty()) {
            errors.add("Missing filing history documents for certified copy item " + item.getId() + ".");
            return;
        }
        final var filingHistoryDocument = (Map<String, Object>) filingHistoryDocuments.get(0);
        MANDATORY_CERTIFIED_COPY_FILING_HISTORY_DOCUMENT_FIELDS.keySet()
                .forEach(field -> checkForBlankFilingHistoryDocumentField(
                        item,
                        errors,
                        filingHistoryDocument,
                        field,
                        MANDATORY_CERTIFIED_COPY_FILING_HISTORY_DOCUMENT_FIELDS.get(field)));
    }

    private void checkForBlankFilingHistoryDocumentField(final Item item,
                                                         final List<String> errors,
                                                         final Map<String, Object> document,
                                                         final String jsonFieldName,
                                                         final String errorFieldName) {
        final var field = (String) document.get(jsonFieldName);
        if (isBlank(field)) {
            errors.add("Missing filing history " + errorFieldName + " for certified copy item " + item.getId() + ".");
        }
    }
}