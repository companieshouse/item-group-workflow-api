package uk.gov.companieshouse.itemgroupworkflowapi.repository;

import java.util.List;
import org.springframework.data.mongodb.repository.ExistsQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroup;

@Repository
public interface ItemGroupsRepository extends MongoRepository<ItemGroup, String> {
    @ExistsQuery("{ 'data.items.id': { $exists: true, $in: ?0 } }")
    boolean existItemGroupsWithSameItems(List<String> itemIds);
}