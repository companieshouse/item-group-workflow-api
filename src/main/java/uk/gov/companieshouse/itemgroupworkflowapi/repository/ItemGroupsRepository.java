package uk.gov.companieshouse.itemgroupworkflowapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroupCreate;

@Repository
public interface ItemGroupsRepository extends MongoRepository<ItemGroupCreate, String> {
}