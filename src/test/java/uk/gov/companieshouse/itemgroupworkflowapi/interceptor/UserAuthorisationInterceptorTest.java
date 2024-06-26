package uk.gov.companieshouse.itemgroupworkflowapi.interceptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtils;
import uk.gov.companieshouse.logging.Logger;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.TestConstants.ERIC_AUTHORISED_ROLES_HEADER_NAME;

@ExtendWith(MockitoExtension.class)
class UserAuthorisationInterceptorTest {
    @InjectMocks
    private UserAuthorisationInterceptor userAuthorisationInterceptor;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private LoggingUtils loggingUtils;
    @Mock
    private Logger logger;

    @BeforeEach
    void beforeEach() {
        when(loggingUtils.logger()).thenReturn(logger);
    }

    @Test
    @DisplayName("preHandle ERIC-Authorised-Key-Roles is present and CORRECT value")
    void willAuthoriseIfEricHeadersArePresentAndCorrectValue() {
        lenient()
                .doReturn("*")
                .when(request)
                .getHeader(ERIC_AUTHORISED_ROLES_HEADER_NAME);

        assertTrue(userAuthorisationInterceptor.preHandle(request, response, null));
    }
    @Test
    @DisplayName("preHandle ERIC-Authorised-Key-Roles is MISSING")
    void willNotAuthoriseIfEricHeadersAreMissing() {
        lenient()
                .doReturn(null)
                .when(request)
                .getHeader(ERIC_AUTHORISED_ROLES_HEADER_NAME);

        assertFalse(userAuthorisationInterceptor.preHandle(request, response, null));
    }
    @Test
    @DisplayName("preHandle ERIC-Authorised-Key-Roles is present and INCORRECT value")
    void willNotAuthoriseIfEricHeadersPresentAndIncorrectValue() {
        lenient()
                .doReturn("xxx")
                .when(request)
                .getHeader(ERIC_AUTHORISED_ROLES_HEADER_NAME);

        assertFalse(userAuthorisationInterceptor.preHandle(request, response, null));
    }
}