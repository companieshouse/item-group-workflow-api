package uk.gov.companieshouse.itemgroupworkflowapi.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.TestConstants.AVRO_ITEM_GROUP_PROCESSED;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.TestConstants.ITEM;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.TestConstants.ITEM_GROUP;

import consumer.deserialization.AvroDeserializer;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.companieshouse.itemgroupprocessed.ItemGroupProcessed;
import uk.gov.companieshouse.itemgroupworkflowapi.config.KafkaConfig;
import uk.gov.companieshouse.itemgroupworkflowapi.kafka.ItemGroupProcessedFactory;
import uk.gov.companieshouse.itemgroupworkflowapi.service.ItemGroupProcessedProducerService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

/**
 * Integration tests the {@link ItemGroupProcessedProducerService}.
 */
@SpringBootTest
@SpringJUnitConfig(classes = {
    KafkaConfig.class,
    ItemGroupProcessedProducerService.class,
    ItemGroupProcessedFactory.class,
    ItemGroupProcessedProducerServiceIntegrationTest.Config.class
})
@EmbeddedKafka
class ItemGroupProcessedProducerServiceIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        "ItemGroupProcessedProducerServiceIntegrationTest");
    private static final String ITEM_GROUP_PROCESSED_TOPIC = "item-group-processed";

    private static final int MESSAGE_WAIT_TIMEOUT_SECONDS = 10;

    private static final ItemGroupProcessed EXPECTED_ITEM_GROUP_PROCESSED_MESSAGE = AVRO_ITEM_GROUP_PROCESSED;

    @Autowired
    private ItemGroupProcessedProducerService serviceUnderTest;

    @MockBean
    private Logger logger;

    private final CountDownLatch messageReceivedLatch = new CountDownLatch(1);
    private ItemGroupProcessed messageReceived;

    @Configuration
    @EnableKafka
    static class Config {

        @Bean
        public ConsumerFactory<String, ItemGroupProcessed> consumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
            return new DefaultKafkaConsumerFactory<>(
                Map.of(
                    ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                    ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                    ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                    ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class,
                    ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, AvroDeserializer.class,
                    ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                    ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"),
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(
                    new AvroDeserializer<>(ItemGroupProcessed.class)));
        }

        @Bean
        public ConcurrentKafkaListenerContainerFactory<String, ItemGroupProcessed> kafkaListenerContainerFactory(
            ConsumerFactory<String, ItemGroupProcessed> consumerFactory) {
            ConcurrentKafkaListenerContainerFactory<String, ItemGroupProcessed> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
            factory.setConsumerFactory(consumerFactory);
            factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
            return factory;
        }

    }

    @Test
    @DisplayName("ItemGroupProcessedProducerService produces a message to item-group-processed successfully")
    void producesMessageSuccessfully() throws InterruptedException {

        serviceUnderTest.sendMessage(ITEM, ITEM_GROUP);

        verifyExpectedMessageIsReceived();
    }

    @KafkaListener(topics = ITEM_GROUP_PROCESSED_TOPIC, groupId = "test-group")
    public void receiveMessage(final @Payload ItemGroupProcessed message) {
        LOGGER.info("Received message: " + message);
        messageReceived = message;
        messageReceivedLatch.countDown();
    }

    private void verifyExpectedMessageIsReceived() throws InterruptedException {
        verifyWhetherMessageIsReceived(true);
        assertThat(messageReceived, is(notNullValue()));
        assertThat(Objects.deepEquals(messageReceived, EXPECTED_ITEM_GROUP_PROCESSED_MESSAGE),
            is(true));
    }

    private void verifyWhetherMessageIsReceived(final boolean messageIsReceived)
        throws InterruptedException {
        LOGGER.info(
            "Waiting to receive message for up to " + MESSAGE_WAIT_TIMEOUT_SECONDS + " seconds.");
        final var received = messageReceivedLatch.await(MESSAGE_WAIT_TIMEOUT_SECONDS,
            TimeUnit.SECONDS);
        assertThat(received, is(messageIsReceived));
    }

}
