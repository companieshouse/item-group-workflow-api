package uk.gov.companieshouse.itemgroupworkflowapi.controller;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtils;
import uk.gov.companieshouse.itemgroupworkflowapi.validation.ItemGroupsValidator;
import uk.gov.companieshouse.logging.Logger;

import javax.servlet.http.HttpServletRequest;

class ItemGroupControllerTest {
    @InjectMocks
    ItemGroupController controller;

    @Mock
    private LoggingUtils loggingUtils;

    @Mock
    private Logger logger;

    @Mock
    private HttpServletRequest request;

    @Mock
    private ItemGroupsValidator requestValidator;

    @Test
    void createItemGroupTest() {
    }
}