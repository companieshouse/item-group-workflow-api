package uk.gov.companieshouse.itemgroupworkflowapi.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroup;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroupData;
import uk.gov.companieshouse.itemgroupworkflowapi.service.ItemGroupsService;
import uk.gov.companieshouse.itemgroupworkflowapi.validation.ItemGroupsValidator;
import uk.gov.companieshouse.itemgroupworkflowapi.util.TestConstants;
import uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtils;
import uk.gov.companieshouse.logging.Logger;

/**
 * Unit tests the {@link ItemGroupController} class.
 */
@ExtendWith(MockitoExtension.class)
class ItemGroupControllerTest {

    @InjectMocks
    private ItemGroupController controller;

    @Mock
    private ItemGroupData itemGroupData;

    @Mock
    private ItemGroupsService itemGroupsService;

    @Mock
    private ItemGroupsValidator itemGroupsValidator;

    @Mock
    private Logger logger;

    @Mock
    private LoggingUtils loggingUtils;

    @Test
    @DisplayName("Create ItemGroup is successful")
    void createItemGroupSuccessful() {

        ItemGroupData itemGroupData = new ItemGroupData();
        ItemGroup itemGroup = new ItemGroup();

        when(itemGroupsService.createItemGroup(itemGroupData)).thenReturn(itemGroup);

        ResponseEntity<Object> response =
                controller.createItemGroup(TestConstants.TOKEN_REQUEST_ID_VALUE, itemGroupData);

        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
        assertThat(response.getBody(), is(response));
    }

    @Test
    @DisplayName("Create ItemGroup has validation errors")
    void createItemGroupValidationErrors()  {

        // TO-DO IN HERE

        ResponseEntity<Object> response =
                controller.createItemGroup(TestConstants.TOKEN_REQUEST_ID_VALUE, itemGroupData);

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }
}


