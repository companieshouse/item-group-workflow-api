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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

        setUpItemWithId("IT1");

        final var itemGot = serviceUnderTest.getItem("IG1", "IT1");
        assertThat(itemGot, is(item));
    }

    @Test
    @DisplayName("getItem() throws ItemNotFoundException for unknown group")
    void getItemThrowsItemNotFoundExceptionForUnknownGroup() {

        when(itemGroupsRepository.findById(anyString())).thenReturn(Optional.empty());
        when(loggingUtils.logger()).thenReturn(logger);

        final var exception = assertThrows(ItemNotFoundException.class,
                () -> serviceUnderTest.getItem("IG1", "IT1"));
        assertThat(exception.getMessage(), Matchers.is("Not able to find item IT1 in group IG1."));
    }

    @Test
    @DisplayName("getItem() throws ItemNotFoundException for unknown item")
    void getItemThrowsItemNotFoundExceptionForUnknownItem() {

        setUpItemWithId("unknown");
        when(loggingUtils.logger()).thenReturn(logger);

        final var exception = assertThrows(ItemNotFoundException.class,
                () -> serviceUnderTest.getItem("IG1", "IT1"));
        assertThat(exception.getMessage(), Matchers.is("Not able to find item IT1 in group IG1."));
    }

    @Test
    @DisplayName("updateItem updates item successfully")
    void updateItemUpdatesItemSuccessfully() {

        setUpItemWithId("IT1");
        when(itemGroupsRepository.save(group)).thenReturn(group);

        final var savedItem = serviceUnderTest.updateItem("IG1", "IT1", item);
        verifyItemAndGroupUpdated(savedItem);
    }

    @Test
    @DisplayName("updateItem() throws ItemNotFoundException for unknown group")
    void updateItemThrowsItemNotFoundExceptionForUnknownGroup() {

        when(itemGroupsRepository.findById(anyString())).thenReturn(Optional.empty());
        when(loggingUtils.logger()).thenReturn(logger);

        final var exception = assertThrows(ItemNotFoundException.class,
                () -> serviceUnderTest.updateItem("IG1", "IT1", item));
        assertThat(exception.getMessage(), Matchers.is("Not able to find item IT1 in group IG1."));
    }

    @Test
    @DisplayName("updateItem() throws ItemNotFoundException for unknown item")
    void updateItemThrowsItemNotFoundExceptionForUnknownItem() {

        setUpItemWithId("unknown");
        when(itemGroupsRepository.save(group)).thenReturn(group);
        when(group.getId()).thenReturn("IG1");
        when(loggingUtils.logger()).thenReturn(logger);

        final var exception = assertThrows(ItemNotFoundException.class,
                () -> serviceUnderTest.updateItem("IG1", "IT1", item));
        assertThat(exception.getMessage(), Matchers.is("Not able to find item IT1 in group IG1."));
    }

    private void verifyItemAndGroupUpdated(final Item savedItem) {
        assertThat(savedItem, is(item));
        verify(item).setUpdatedAt(any(LocalDateTime.class));
        verify(item, times(2)).getId();
        verify(group).setUpdatedAt(any(LocalDateTime.class));
        verify(group, times(2)).getData();
        verify(itemGroupData).setItems(anyList());
    }

    private void setUpItemWithId(final String itemId) {
        when(itemGroupsRepository.findById(anyString())).thenReturn(Optional.of(group));
        when(group.getData()).thenReturn(itemGroupData);
        when(itemGroupData.getItems()).thenReturn(Collections.singletonList(item));
        when(item.getId()).thenReturn(itemId);
    }

}
