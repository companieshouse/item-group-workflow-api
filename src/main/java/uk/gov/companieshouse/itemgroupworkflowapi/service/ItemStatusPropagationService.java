package uk.gov.companieshouse.itemgroupworkflowapi.service;

import static java.util.Collections.singletonList;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMessage;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.itemgroupworkflowapi.dto.ItemDto;
import uk.gov.companieshouse.itemgroupworkflowapi.dto.ItemStatusUpdateDto;
import uk.gov.companieshouse.itemgroupworkflowapi.exception.ItemStatusUpdatePropagationException;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Item;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroup;
import uk.gov.companieshouse.itemgroupworkflowapi.validation.Status;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.util.DataMap;

/**
 * Service that propagates the <b><code>satisfied</code></b> status of an item in an item group via
 * the <code>chs-kafka-api</code>. The latter in turn produces
 * <code>item-group-processed-send</code> Kafka messages to be consumed by the
 * <code>order-notification-sender</code>.
 */
@Service
public class ItemStatusPropagationService {

    private static final String SATISFIED = Status.SATISFIED.toString();

    public static final String ITEM_STATUS_UPDATED_URL = "/private/item-group-processed-send";

    private final RestTemplate restTemplate;

    private final Logger logger;

    private final String chsKafkaApiUrl;

    public ItemStatusPropagationService(
        RestTemplate restTemplate,
        Logger logger,
        @Value("${chs.kafka.api.url}") String chsKafkaApiUrl) {
        this.restTemplate = restTemplate;
        this.logger = logger;
        this.chsKafkaApiUrl = chsKafkaApiUrl;
    }

    public void propagateItemSatisfiedStatusUpdate(final Item updatedItem,
        final ItemGroup itemGroup) {

        final var groupItem = getGroupItem(itemGroup, updatedItem);
        final var orderNumber = itemGroup.getData().getOrderNumber();
        final var status = updatedItem.getStatus();

        if (!status.equals(SATISFIED)) {
            logger.info("Item status update propagation SUPPRESSED for order number "
                    + orderNumber + ", group item " + groupItem + ", because the updated status `" +
                    status + "` is not `" + SATISFIED + "`.",
                getLogMap(orderNumber, itemGroup.getId(), updatedItem.getId()));
            return;
        }

        final var item = new ItemDto(
            updatedItem.getId(),
            status,
            updatedItem.getDigitalDocumentLocation());
        final var update = new ItemStatusUpdateDto(orderNumber, groupItem, item);
        final var headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        headers.setAccept(singletonList(APPLICATION_JSON));
        final HttpEntity<ItemStatusUpdateDto> httpEntity = new HttpEntity<>(update, headers);

        try {
            restTemplate.exchange(
                chsKafkaApiUrl + ITEM_STATUS_UPDATED_URL,
                HttpMethod.POST,
                httpEntity,
                HttpMessage.class);
            logger.info("Item status update propagation successful for order number "
                    + orderNumber + ", group item " + groupItem + ".",
                getLogMap(orderNumber, itemGroup.getId(), updatedItem.getId()));
        } catch (RestClientException rce) {
            final String error = "Item status update propagation FAILED for order number "
                + orderNumber + ", group item " + groupItem
                + ", caught RestClientException with message "
                + rce.getMessage() + ".";
            logger.error(error,
                getLogMap(orderNumber, itemGroup.getId(), updatedItem.getId(), rce.getMessage()));
            throw new ItemStatusUpdatePropagationException(error);
        }
    }

    private String getGroupItem(final ItemGroup group, final Item item) {
        return "/item-groups/" + group.getId() + "/items/" + item.getId();
    }

    private Map<String, Object> getLogMap(
        final String orderNumber,
        final String itemGroupId,
        final String itemId) {
        return new DataMap.Builder()
            .orderId(orderNumber)
            .itemGroupId(itemGroupId)
            .itemId(itemId)
            .build()
            .getLogMap();
    }

    private Map<String, Object> getLogMap(
        final String orderNumber,
        final String itemGroupId,
        final String itemId,
        final String error) {
        return new DataMap.Builder()
            .orderId(orderNumber)
            .itemGroupId(itemGroupId)
            .itemId(itemId)
            .errors(singletonList(error))
            .build()
            .getLogMap();
    }

}
