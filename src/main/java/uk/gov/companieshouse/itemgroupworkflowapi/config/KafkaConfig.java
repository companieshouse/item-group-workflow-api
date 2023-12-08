package uk.gov.companieshouse.itemgroupworkflowapi.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import uk.gov.companieshouse.itemgroupprocessed.ItemGroupProcessed;
import uk.gov.companieshouse.itemgroupworkflowapi.kafka.ItemGroupProcessedAvroSerializer;
import uk.gov.companieshouse.itemgroupworkflowapi.kafka.ItemOrderedCertifiedCopyAvroSerializer;
import uk.gov.companieshouse.itemorderedcertifiedcopy.ItemOrderedCertifiedCopy;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    public ProducerFactory<String, ItemOrderedCertifiedCopy> producerFactory(
            @Value("${spring.kafka.bootstrap-servers}" ) final String bootstrapServers) {
        final Map<String, Object> config = new HashMap<>();
        config.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ItemOrderedCertifiedCopyAvroSerializer.class);
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, ItemOrderedCertifiedCopy> kafkaTemplate(
            @Value("${spring.kafka.bootstrap-servers}" ) final String bootstrapServers) {
        return new KafkaTemplate<>(producerFactory(bootstrapServers));
    }

    @Bean
    public ItemOrderedCertifiedCopyAvroSerializer avroSerializer() {
        return new ItemOrderedCertifiedCopyAvroSerializer();
    }

    @Bean
    public ProducerFactory<String, ItemGroupProcessed> itemGroupProcessedProducerFactory(
        @Value("${spring.kafka.bootstrap-servers}" ) final String bootstrapServers) {
        final Map<String, Object> config = new HashMap<>();
        config.put(
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ItemGroupProcessedAvroSerializer.class);
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, ItemGroupProcessed> itemGroupProcessedKafkaTemplate(
        @Value("${spring.kafka.bootstrap-servers}" ) final String bootstrapServers) {
        return new KafkaTemplate<>(itemGroupProcessedProducerFactory(bootstrapServers));
    }

    @Bean
    public ItemGroupProcessedAvroSerializer itemGroupProcessedAvroSerializer() {
        return new ItemGroupProcessedAvroSerializer();
    }

}
