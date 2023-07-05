package uk.gov.companieshouse.itemgroupworkflowapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import uk.gov.companieshouse.itemgroupworkflowapi.kafka.ItemOrderedCertifiedCopyFactory;
import uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtils;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Item;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroupData;
import uk.gov.companieshouse.itemorderedcertifiedcopy.ItemOrderedCertifiedCopy;
import uk.gov.companieshouse.logging.Logger;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.itemgroupworkflowapi.model.ItemKind.ITEM_CERTIFICATE;
import static uk.gov.companieshouse.itemgroupworkflowapi.model.ItemKind.ITEM_CERTIFIED_COPY;
import static uk.gov.companieshouse.itemgroupworkflowapi.model.ItemKind.ITEM_MISSING_IMAGE_DELIVERY;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.Constants.ITEM_ORDERED_CERTIFIED_COPY_TOPIC;

/**
 * Unit tests the {@link KafkaProducerService} class.
 */
@ExtendWith(MockitoExtension.class)
class KafkaProducerServiceTest {

    @InjectMocks
    private KafkaProducerService service;

    @Mock
    private ItemGroupData group;

    @Mock
    private KafkaTemplate<String, ItemOrderedCertifiedCopy> kafkaTemplate;

    @Mock
    private LoggingUtils loggingUtils;

    @Mock
    private Logger logger;

    @Mock
    private ItemOrderedCertifiedCopyFactory certifiedCopyFactory;

    @Mock
    private ListenableFuture<SendResult<String, ItemOrderedCertifiedCopy>> future;

    @Mock
    private ItemOrderedCertifiedCopy message;

    @BeforeEach
    void setUp() {
        when(loggingUtils.getLogger()).thenReturn(logger);
        service = new KafkaProducerService(kafkaTemplate, loggingUtils, certifiedCopyFactory);
        service.afterPropertiesSet();
    }

    @Test
    @DisplayName("A message is produced for a certified copy item")
    void messageProducedForCertifiedCopyItem() {

        final var item = new Item();
        item.setId("123");
        item.setKind(ITEM_CERTIFIED_COPY.toString());
        when(group.getItems()).thenReturn(Collections.singletonList(item));
        when(certifiedCopyFactory.buildMessage(group, item)).thenReturn(message);
        when(kafkaTemplate.send(eq(ITEM_ORDERED_CERTIFIED_COPY_TOPIC), any(ItemOrderedCertifiedCopy.class)))
                .thenReturn(future);

        service.produceMessages(group);

        verify(kafkaTemplate).send(eq(ITEM_ORDERED_CERTIFIED_COPY_TOPIC), any(ItemOrderedCertifiedCopy.class));
    }

    @Test
    @DisplayName("No message is produced for a certificate item")
    void noMessageProducedForCertificateItem() {

        final var item = new Item();
        item.setId("123");
        item.setKind(ITEM_CERTIFICATE.toString());
        when(group.getItems()).thenReturn(Collections.singletonList(item));

        service.produceMessages(group);

        verify(kafkaTemplate, never()).send(eq(ITEM_ORDERED_CERTIFIED_COPY_TOPIC), any(ItemOrderedCertifiedCopy.class));
    }

    @Test
    @DisplayName("No message is produced for a MID item")
    void noMessageProducedForMidItem() {

        final var item = new Item();
        item.setId("123");
        item.setKind(ITEM_MISSING_IMAGE_DELIVERY.toString());
        when(group.getItems()).thenReturn(Collections.singletonList(item));

        service.produceMessages(group);

        verify(kafkaTemplate, never()).send(eq(ITEM_ORDERED_CERTIFIED_COPY_TOPIC), any(ItemOrderedCertifiedCopy.class));
    }

}
