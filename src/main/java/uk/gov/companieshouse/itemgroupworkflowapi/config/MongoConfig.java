package uk.gov.companieshouse.itemgroupworkflowapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
/**
 * Custom configuration for Mongo added so that _class attributes are not saved on objects stored in MongoDB.
 * Taken from uk.gov.companieshouse.certificates.orders.api.config => MongoConfig.java
 * NOTE custom conversions String->enum and enum->String are NOT included.
 */
@Configuration
public class MongoConfig {
    /**
     * _class maps to the model class in mongoDB (e.g. _class : uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroup)
     * when using spring data mongo it by default adds a _class key to your collection to be able to
     * handle inheritance. But if your domain model is simple and flat, you can remove it by overriding
     * the default MappingMongoConverter.
     */
    @Bean
    public MappingMongoConverter mappingMongoConverter(final MongoDatabaseFactory factory,
                                                       final MongoMappingContext context) {
        final MappingMongoConverter mappingConverter = new MappingMongoConverter(
            new DefaultDbRefResolver(factory),
            context);
        //
        // typeKey == null so _class not saved.
        //
        mappingConverter.setTypeMapper(new DefaultMongoTypeMapper(null));

        return mappingConverter;
    }
}