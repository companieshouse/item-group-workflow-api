package uk.gov.companieshouse.itemgroupworkflowapi.service;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.itemgroupworkflowapi.exception.ItemNotFoundException;
import uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtils;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Item;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroup;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroupData;
import uk.gov.companieshouse.itemgroupworkflowapi.repository.ItemGroupsRepository;
import uk.gov.companieshouse.logging.Logger;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests the {@link ItemGroupsService} class.
 */
@ExtendWith(MockitoExtension.class)
class ItemGroupsServiceTest {

    @InjectMocks
    private ItemGroupsService serviceUnderTest;

    @Mock
    private ItemGroupsRepository itemGroupsRepository;

    @Mock
    private LoggingUtils loggingUtils;

    @Mock
    private ItemGroup group;

    @Mock
    private ItemGroupData itemGroupData;

    @Mock
    private Item item;

    @Mock
    private Logger logger;

    @Test
    @DisplayName("getItem() gets item successfully")
    void getItemGetsItemSuccessfully() {

        when(itemGroupsRepository.findById(anyString())).thenReturn(Optional.of(group));
        when(group.getData()).thenReturn(itemGroupData);
        when(itemGroupData.getItems()).thenReturn(Collections.singletonList(item));
        when(item.getId()).thenReturn("IT1");

        final var itemGot = serviceUnderTest.getItem("IG1", "IT1");
        assertThat(itemGot, is(item));
    }

    @Test
    @DisplayName("getItem() throws ItemNotFoundException for unknown group")
    void getItemThrowsItemNotFoundExceptionForUnknownGroup() {

        when(itemGroupsRepository.findById(anyString())).thenReturn(Optional.empty());
        when(loggingUtils.getLogger()).thenReturn(logger);

        final ItemNotFoundException exception = assertThrows(ItemNotFoundException.class,
                () -> serviceUnderTest.getItem("IG1", "IT1"));
        assertThat(exception.getMessage(), Matchers.is("Not able to find item IT1 in group IG1."));
    }

    @Test
    @DisplayName("getItem() throws ItemNotFoundException for unknown item")
    void getItemThrowsItemNotFoundExceptionForUnknownItem() {

        when(itemGroupsRepository.findById(anyString())).thenReturn(Optional.of(group));
        when(group.getData()).thenReturn(itemGroupData);
        when(itemGroupData.getItems()).thenReturn(Collections.singletonList(item));
        when(item.getId()).thenReturn("unknown");
        when(loggingUtils.getLogger()).thenReturn(logger);

        final ItemNotFoundException exception = assertThrows(ItemNotFoundException.class,
                () -> serviceUnderTest.getItem("IG1", "IT1"));
        assertThat(exception.getMessage(), Matchers.is("Not able to find item IT1 in group IG1."));
    }

}
