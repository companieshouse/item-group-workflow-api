package uk.gov.companieshouse.itemgroupworkflowapi.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.itemgroupworkflowapi.model.TestDTO;
import uk.gov.companieshouse.itemgroupworkflowapi.repository.ItemGroupsRepository;

@Service
public class ItemGroupsService {
    private final ItemGroupsRepository itemGroupsRepository;

    public ItemGroupsService(ItemGroupsRepository itemGroupsRepository) {
        this.itemGroupsRepository = itemGroupsRepository;
    }

    public TestDTO saveTestDto(TestDTO testDto) {
        final TestDTO savedTestDto = itemGroupsRepository.save(testDto);
        return savedTestDto;
    }
}
