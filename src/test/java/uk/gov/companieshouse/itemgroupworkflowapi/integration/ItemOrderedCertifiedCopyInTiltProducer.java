package uk.gov.companieshouse.itemgroupworkflowapi.integration;

import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import uk.gov.companieshouse.itemorderedcertifiedcopy.ItemOrderedCertifiedCopy;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.companieshouse.itemgroupworkflowapi.util.TestConstants.CERTIFIED_COPY;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.TestConstants.ITEM_ORDERED_CERTIFIED_COPY_TOPIC;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.TestConstants.SAME_PARTITION_KEY;

/**
 * "Test" class re-purposed to produce {@link ItemOrderedCertifiedCopy} messages to the
 * <code>item-ordered-certified-copy</code> topic in Tilt. This is NOT to be run as part
 * of an automated test suite. It is for manual testing only.
 */
@SpringBootTest
@TestPropertySource(properties = "spring.kafka.bootstrap-servers=${BOOTSTRAP_SERVER_URL}")
@ActiveProfiles("manual")
@SuppressWarnings("squid:S3577") // This is NOT to be run as part of an automated test suite.
class ItemOrderedCertifiedCopyInTiltProducer {

    private static final String KAFKA_IN_TILT_BOOTSTRAP_SERVER_URL = "localhost:29092";

    @Rule
    private static final EnvironmentVariables ENVIRONMENT_VARIABLES;

    static {
        ENVIRONMENT_VARIABLES = new EnvironmentVariables();
        ENVIRONMENT_VARIABLES.set("BOOTSTRAP_SERVER_URL", KAFKA_IN_TILT_BOOTSTRAP_SERVER_URL);
    }

    public static class KafkaTemplateAvroSerializer implements Serializer<ItemOrderedCertifiedCopy> {

        @Override
        public byte[] serialize(String topic, ItemOrderedCertifiedCopy data) {
            DatumWriter<ItemOrderedCertifiedCopy> datumWriter = new SpecificDatumWriter<>();

            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                Encoder encoder = EncoderFactory.get().binaryEncoder(out, null);
                datumWriter.setSchema(data.getSchema());
                datumWriter.write(data, encoder);
                encoder.flush();

                byte[] serializedData = out.toByteArray();
                encoder.flush();

                return serializedData;
            } catch (IOException e) {
                throw new SerializationException("Error when serializing ItemOrderedCertifiedCopy to byte[]");
            }
        }
    }

    @Configuration
    @Profile("manual")
    static class KafkaProducerConfig {

        @Bean
        KafkaProducer<String, ItemOrderedCertifiedCopy> testProducer(
                @Value("${spring.kafka.bootstrap-servers}") final String bootstrapServers) {
            return new KafkaProducer<>(
                    Map.of(
                            ProducerConfig.ACKS_CONFIG, "all",
                            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers),
                    new StringSerializer(),
                    (topic, data) -> {
                        try {
                            return new SerializerFactory()
                                    .getSpecificRecordSerializer(ItemOrderedCertifiedCopy.class)
                                    .toBinary(data); //creates a leading space
                        } catch (uk.gov.companieshouse.kafka.exceptions.SerializationException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }

    }

    @Configuration
    @Profile("manual")
    static class KafkaTemplateConfig {

        @Bean
        public ProducerFactory<String, ItemOrderedCertifiedCopy> producerFactory() {
            Map<String, Object> config = new HashMap<>();
            config.put(
                    ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                    StringSerializer.class);
            config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaTemplateAvroSerializer.class);
            config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_IN_TILT_BOOTSTRAP_SERVER_URL);
            return new DefaultKafkaProducerFactory<>(config);
        }

        @Bean
        public KafkaTemplate<String, ItemOrderedCertifiedCopy> kafkaTemplate() {
            return new KafkaTemplate<>(producerFactory());
        }

        @Bean
        public KafkaTemplateAvroSerializer avroSerializer() {
            return new KafkaTemplateAvroSerializer();
        }

    }

    @Autowired
    private KafkaProducer<String, ItemOrderedCertifiedCopy> testProducer;


    @Autowired
    private KafkaTemplate<String, ItemOrderedCertifiedCopy> testTemplate;


    @SuppressWarnings("squid:S2699") // at least one assertion
    @Test
    void produceMessageToTiltThroughKafkaProducer() {
        testProducer.send(new ProducerRecord<>(
                ITEM_ORDERED_CERTIFIED_COPY_TOPIC, 0, System.currentTimeMillis(), SAME_PARTITION_KEY, CERTIFIED_COPY));
    }

    @SuppressWarnings("squid:S2699") // at least one assertion
    @Test
    void produceMessageToTiltThroughKafkaTemplate() {
        testTemplate.send(ITEM_ORDERED_CERTIFIED_COPY_TOPIC, CERTIFIED_COPY);
    }
}
