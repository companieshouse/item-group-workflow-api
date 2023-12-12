package uk.gov.companieshouse.itemgroupworkflowapi.config;

import consumer.serialization.AvroSerializer;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import uk.gov.companieshouse.itemgroupprocessed.ItemGroupProcessed;
import uk.gov.companieshouse.itemorderedcertifiedcopy.ItemOrderedCertifiedCopy;

@Configuration
public class KafkaConfig {

    @Bean
    public ProducerFactory<String, ItemOrderedCertifiedCopy> itemOrderedCertifiedCopyProducerFactory(
        @Value("${spring.kafka.bootstrap-servers}") final String bootstrapServers) {
        return new DefaultKafkaProducerFactory<>(createProducerConfig(bootstrapServers));
    }

    @Bean
    public KafkaTemplate<String, ItemOrderedCertifiedCopy> itemOrderedCertifiedCopyKafkaTemplate(
        @Value("${spring.kafka.bootstrap-servers}") final String bootstrapServers) {
        return new KafkaTemplate<>(itemOrderedCertifiedCopyProducerFactory(bootstrapServers));
    }

    @Bean
    public ProducerFactory<String, ItemGroupProcessed> itemGroupProcessedProducerFactory(
        @Value("${spring.kafka.bootstrap-servers}") final String bootstrapServers) {
        return new DefaultKafkaProducerFactory<>(createProducerConfig(bootstrapServers));
    }

    @Bean
    public KafkaTemplate<String, ItemGroupProcessed> itemGroupProcessedKafkaTemplate(
        @Value("${spring.kafka.bootstrap-servers}") final String bootstrapServers) {
        return new KafkaTemplate<>(itemGroupProcessedProducerFactory(bootstrapServers));
    }

    private static Map<String, Object> createProducerConfig(final String bootstrapServers) {
        final Map<String, Object> config = new HashMap<>();
        config.put(
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, AvroSerializer.class);
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return config;
    }

}
