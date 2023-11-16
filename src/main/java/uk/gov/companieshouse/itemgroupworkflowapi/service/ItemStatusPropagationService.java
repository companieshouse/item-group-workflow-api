package uk.gov.companieshouse.itemgroupworkflowapi.service;

import static java.util.Collections.singletonList;
import static org.springframework.http.MediaType.APPLICATION_JSON;

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
import uk.gov.companieshouse.itemgroupworkflowapi.model.Item;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroup;
import uk.gov.companieshouse.logging.Logger;

/**
 * Service that propagates the updated service of an item in an item group via the `chs-kafka-api`,
 * which in turn produces <code>item-group-processed-send</code> Kafka messages to be consumed by the
 * `order-notification-sender`.
 */
@Service
public class ItemStatusPropagationService {

    private static final String ITEM_STATUS_UPDATED_URL = "/private/item-group-processed-send";

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

    public void propagateItemStatusUpdate(final Item updatedItem, final ItemGroup itemGroup) {

        final var headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        headers.setAccept(singletonList(APPLICATION_JSON));
        final var item = new ItemDto(
            updatedItem.getId(),
            updatedItem.getStatus(),
            updatedItem.getDigitalDocumentLocation());
        final var groupItem = getGroupItem(itemGroup, updatedItem);
        final var orderNumber = itemGroup.getData().getOrderNumber();
        final var update = new ItemStatusUpdateDto(orderNumber, groupItem, item);
        final HttpEntity<ItemStatusUpdateDto> httpEntity = new HttpEntity<>(update, headers);

        try {
                restTemplate.exchange(
                    chsKafkaApiUrl + ITEM_STATUS_UPDATED_URL,
                    HttpMethod.POST,
                    httpEntity,
                    HttpMessage.class);
            logger.info("Item status update propagation successful for order number "
                + orderNumber + ", group item " + groupItem + ".");
        } catch (RestClientException rce) {
            // Exception is NOT rethrown as the clients will not be able to recover from or retry.
            logger.error("Item status update propagation FAILED for order number "
                + orderNumber + ", group item " + groupItem + ", caught RestClientException with message "
                + rce.getMessage() + ".");
        }
    }

    private String getGroupItem(final ItemGroup group, final Item item) {
        return "/item-groups/" + group.getId() + "/items/" + item.getId();
    }

}