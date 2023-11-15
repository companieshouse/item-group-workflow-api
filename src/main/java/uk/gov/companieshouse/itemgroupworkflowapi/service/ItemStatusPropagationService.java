package uk.gov.companieshouse.itemgroupworkflowapi.service;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;
import uk.gov.companieshouse.itemgroupworkflowapi.dto.ItemDto;
import uk.gov.companieshouse.itemgroupworkflowapi.dto.ItemStatusUpdateDto;
import uk.gov.companieshouse.logging.Logger;

/**
 * Service that propagates the updated service of an item in an item group via the `chs-kafka-api`,
 * which in turn produces <code>item-group-processed-send</code> Kafka messages to be consumed by the
 * `order-notification-sender`.
 */
@Service
public class ItemStatusPropagationService {

    private static final UriTemplate ITEM_STATUS_UPDATED_URL = new UriTemplate("/private/item-group-processed-send");

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

    // TODO DCAC-241 Name etc
    public void propagate() {

        final String uri = ITEM_STATUS_UPDATED_URL.expand().toString();
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        final var item = new ItemDto(
            "CCD-768116-5180948",
            "satisfied",
            "s3://document-api-images-cidev/docs/--EdB7fbldt5oujK6Nz7jZ3hGj_x6vW8Q_2gQTyjWBM/application-pdf");
        final var update =
            new ItemStatusUpdateDto("ORD-175015-948111", "/item-groups/IG-305816-983218/items/CCD-768116-5180948", item);
        final HttpEntity<ItemStatusUpdateDto> httpEntity = new HttpEntity<>(update, headers);

        restTemplate.setMessageConverters(getJsonMessageConverters());

        final var message =
            restTemplate.exchange(
                chsKafkaApiUrl + uri,
                HttpMethod.POST,
                httpEntity,
                HttpMessage.class);

        // TODO DCAC 241 return outcome?

    }

    private List<HttpMessageConverter<?>> getJsonMessageConverters() {
        final List<HttpMessageConverter<?>> converters = new ArrayList<>();
        final var converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper( createObjectMapper());
        converters.add(converter);
        return converters;
    }

    private ObjectMapper createObjectMapper() {
        final var objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        return objectMapper;
    }

}
