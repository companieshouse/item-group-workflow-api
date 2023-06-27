package uk.gov.companieshouse.itemgroupworkflowapi.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import uk.gov.companieshouse.itemgroupworkflowapi.kafka.ItemOrderedCertifiedCopyAvroSerializer;
import uk.gov.companieshouse.itemorderedcertifiedcopy.ItemOrderedCertifiedCopy;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    // TODO DCAC-68 Replace this with an environment variable
    private static final String KAFKA_IN_TILT_BOOTSTRAP_SERVER_URL = "kafka:9092";

    @Bean
    public ProducerFactory<String, ItemOrderedCertifiedCopy> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ItemOrderedCertifiedCopyAvroSerializer.class);
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_IN_TILT_BOOTSTRAP_SERVER_URL);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, ItemOrderedCertifiedCopy> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ItemOrderedCertifiedCopyAvroSerializer avroSerializer() {
        return new ItemOrderedCertifiedCopyAvroSerializer();
    }

}
