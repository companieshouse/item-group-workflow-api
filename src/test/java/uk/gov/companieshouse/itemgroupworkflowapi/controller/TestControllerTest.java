package uk.gov.companieshouse.itemgroupworkflowapi.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtilsConfiguration.APPLICATION_NAMESPACE;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.itemgroupworkflowapi.dto.TestDTO;
import uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtils;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class TestControllerTest {
    private static final String TOKEN_COMPANY_NAME = "ACME Inc";
    private static final String TOKEN_COMPANY_NUMBER = "1234567890";
    @Mock
    private LoggingUtils loggingUtils;
    @Mock
    private Logger logger;
    @InjectMocks
    private TestController controller;

    @BeforeEach
    void beforeEach() {
        when(loggingUtils.getLogger()).thenReturn(logger);
    }
    @AfterEach
    void afterEach() {
        logger = null;
    }

    @Test
    void rootCheck() {
        final ResponseEntity<String> response = controller.rootCheck();
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(APPLICATION_NAMESPACE));
    }

    @Test
    void get201response() {
        final ResponseEntity<Void> response = controller.get201response();  // GET
        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
    }

    @Test
    void get401response() {
        final ResponseEntity<Void> response = controller.get401response();  // GET
        assertThat(response.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void dtoTestPost() {
        //
        // Init the DTO
        //
        final TestDTO testDTO = new TestDTO();
        testDTO.setCompanyNumber(TOKEN_COMPANY_NUMBER);
        testDTO.setCompanyName(TOKEN_COMPANY_NAME);
        //
        // Make the call and ensure we have HttpStatus.CREATED
        //
        final ResponseEntity<Object> response = controller.dtoTestPost(testDTO);    // POST
        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
        //
        // Check we got the correct DTO values back.
        //
        final TestDTO resultDTO = (TestDTO) response.getBody();
        assert(resultDTO != null);
        assertThat(resultDTO.getCompanyNumber(), is(TOKEN_COMPANY_NUMBER));
        assertThat(resultDTO.getCompanyName(), is(TOKEN_COMPANY_NAME));
    }
}
