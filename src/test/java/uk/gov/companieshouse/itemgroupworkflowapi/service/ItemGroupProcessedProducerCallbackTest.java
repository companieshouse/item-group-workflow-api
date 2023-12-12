package uk.gov.companieshouse.itemgroupworkflowapi.service;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.TestConstants.AVRO_ITEM_GROUP_PROCESSED;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.SendResult;
import uk.gov.companieshouse.itemgroupprocessed.ItemGroupProcessed;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class ItemGroupProcessedProducerCallbackTest {

    private ItemGroupProcessedProducerCallback callback;

    @Mock
    private Logger logger;

    @Mock
    private SendResult<String, ItemGroupProcessed> result;

    @BeforeEach
    void setUp() {
        callback = new ItemGroupProcessedProducerCallback(AVRO_ITEM_GROUP_PROCESSED,
            "item-group-processed", logger);
    }

    @Test
    @DisplayName("onFailure() logs the error clearly")
    void onFailureLogsErrorClearly() {

        // when
        callback.onFailure(new RuntimeException("Test generated exception"));

        // then
        verify(logger).error(eq("Unable to deliver message " + AVRO_ITEM_GROUP_PROCESSED
            + " to topic item-group-processed. Error: Test generated exception."), anyMap());
    }

    @Test
    @DisplayName("onSuccess() logs message delivery clearly")
    void onSuccessLogsDeliveryClearly() {

        // given
        final TopicPartition topicPartition = new TopicPartition("item-group-processed", 0);
        final RecordMetadata metadata = new RecordMetadata(topicPartition, 0, 0, 0, 0, 0);
        when(result.getRecordMetadata()).thenReturn(metadata);

        // when
        callback.onSuccess(result);

        // then
        verify(logger).info(eq("Message " + AVRO_ITEM_GROUP_PROCESSED
            + " delivered to topic item-group-processed on partition 0 with offset 0."), anyMap());
    }
}