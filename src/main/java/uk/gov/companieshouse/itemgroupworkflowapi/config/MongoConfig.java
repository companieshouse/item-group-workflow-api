package uk.gov.companieshouse.itemgroupworkflowapi.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.ArrayList;

@Configuration
public class MongoConfig {
    @Value("${spring.data.mongodb.uri}")
    private String mongoDbConnectionStr;
    @Value("${uk.gov.companieshouse.item.group.workflow.api.database_name}")
    private String databaseName;
    @Value("${uk.gov.companieshouse.item.group.workflow.api.collection_name}")
    private String collectionName;

    public MongoClient mongoClient() {
        return MongoClients.create(mongoDbConnectionStr);
    }

    public @Bean MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), databaseName);
    }
}
