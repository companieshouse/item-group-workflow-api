package uk.gov.companieshouse.itemgroupworkflowapi.integration;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import uk.gov.companieshouse.itemorderedcertifiedcopy.ItemOrderedCertifiedCopy;
import uk.gov.companieshouse.kafka.exceptions.SerializationException;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;

import java.util.Map;

import static uk.gov.companieshouse.itemgroupworkflowapi.util.TestConstants.CERTIFIED_COPY;
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

    @Configuration
    @Profile("manual")
    static class Config {

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
                        } catch (SerializationException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }

    }

    @Autowired
    private KafkaProducer<String, ItemOrderedCertifiedCopy> testProducer;

    @SuppressWarnings("squid:S2699") // at least one assertion
    @Test
    void produceMessageToTilt() {
        testProducer.send(new ProducerRecord<>(
                "item-ordered-certified-copy", 0, System.currentTimeMillis(), SAME_PARTITION_KEY, CERTIFIED_COPY));
    }
}
