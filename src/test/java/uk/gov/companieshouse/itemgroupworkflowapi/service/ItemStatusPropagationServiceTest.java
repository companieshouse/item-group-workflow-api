package uk.gov.companieshouse.itemgroupworkflowapi.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.itemgroupworkflowapi.validation.Status.SATISFIED;

import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMessage;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Item;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroup;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroupData;
import uk.gov.companieshouse.itemgroupworkflowapi.validation.Status;
import uk.gov.companieshouse.logging.Logger;

/**
 * Unit tests the {@link ItemStatusPropagationService} class.
 */
@ExtendWith(MockitoExtension.class)
class ItemStatusPropagationServiceTest {

    @InjectMocks
    private ItemStatusPropagationService itemStatusPropagationService;

    @Mock
    private Item updatedItem;

    @Mock
    private ItemGroup itemGroup;

    @Mock
    private ItemGroupData data;

    @Mock
    private Logger logger;

    @Mock
    private RestTemplate restTemplate;

    @Test
    @DisplayName("SATISFIED status update is propagated on")
    void satisfiedStatusIsPropagated() {

        // Given
        when(itemGroup.getData()).thenReturn(data);
        when(updatedItem.getStatus()).thenReturn(Status.SATISFIED.toString());

        // When
        itemStatusPropagationService.propagateItemSatisfiedStatusUpdate(updatedItem, itemGroup);

        // Then
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(HttpMessage.class));
    }

    @Test
    @DisplayName("non-SATISFIED status updates are not propagated")
    void nonSatisfiedStatusesAreNotPropagated() {
        Arrays.stream(Status.values())
            .filter(status -> status != SATISFIED)
            .forEach(this::nonSatisfiedStatusIsNotPropagated);
    }

    private void nonSatisfiedStatusIsNotPropagated(final Status nonSatisfiedStatus) {

        // Given
        when(itemGroup.getData()).thenReturn(data);
        when(updatedItem.getStatus()).thenReturn(nonSatisfiedStatus.toString());

        // When
        itemStatusPropagationService.propagateItemSatisfiedStatusUpdate(updatedItem, itemGroup);

        // Then
        verify(restTemplate, never()).exchange(anyString(), eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(HttpMessage.class));
    }
}