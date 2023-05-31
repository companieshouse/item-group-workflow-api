package uk.gov.companieshouse.item.group.workflow.api.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.item.group.workflow.api.model.ItemGroup;

@Repository
public interface ItemGroupsRepository extends MongoRepository<ItemGroup, String> {
}
