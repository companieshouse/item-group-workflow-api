package uk.gov.companieshouse.itemgroupworkflowapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroup;

@Repository
public interface ItemGroupsRepository extends MongoRepository<ItemGroup, String> {
    boolean existsItemGroupByDataOrderNumber(String orderNumber);
}