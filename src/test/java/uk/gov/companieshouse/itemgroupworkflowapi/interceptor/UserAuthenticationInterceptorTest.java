package uk.gov.companieshouse.itemgroupworkflowapi.interceptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static uk.gov.companieshouse.itemgroupworkflowapi.util.TestConstants.ERIC_IDENTITY_HEADER_NAME;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.TestConstants.ERIC_IDENTITY_HEADER_VALUE;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.TestConstants.ERIC_IDENTITY_TYPE_HEADER_NAME;

@ExtendWith(MockitoExtension.class)
class UserAuthenticationInterceptorTest {
    @InjectMocks
    private UserAuthenticationInterceptor userAuthenticationInterceptor;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private LoggingUtils loggingUtils;
    @Mock
    private Logger logger;

    @Nested
    class UserAuthenticationInterceptorFailureTest {
        @BeforeEach
        void beforeEach() {
            when(loggingUtils.logger()).thenReturn(logger);
        }

        @Test
        @DisplayName("Authentication : ERIC-Identity header EMPTY")
        void emptyEricIdentityHeader() {
            lenient().doReturn("").when(request).getHeader(ERIC_IDENTITY_HEADER_NAME);
            lenient().doReturn(ERIC_IDENTITY_HEADER_VALUE).when(request).getHeader(ERIC_IDENTITY_TYPE_HEADER_NAME);

            assertFalse(userAuthenticationInterceptor.preHandle(request, response, null));
        }
        @Test
        @DisplayName("Authentication : ERIC-Identity-Type header EMPTY")
        void emptyEricIdentityTypeHeader() {
            lenient().doReturn(ERIC_IDENTITY_HEADER_VALUE).when(request).getHeader(ERIC_IDENTITY_HEADER_NAME);
            lenient().doReturn("").when(request).getHeader(ERIC_IDENTITY_TYPE_HEADER_NAME);

            assertFalse(userAuthenticationInterceptor.preHandle(request, response, null));
        }
        @Test
        @DisplayName("Authentication : ERIC headers EMPTY")
        void emptyBothEricHeaders() {
            lenient().doReturn("").when(request).getHeader(ERIC_IDENTITY_HEADER_NAME);
            lenient().doReturn("").when(request).getHeader(ERIC_IDENTITY_TYPE_HEADER_NAME);

            assertFalse(userAuthenticationInterceptor.preHandle(request, response, null));
        }
        @Test
        @DisplayName("Authentication : ERIC-Identity header MISSING")
        void missingEricIdentityHeader() {
            lenient().doReturn(null).when(request).getHeader(ERIC_IDENTITY_HEADER_NAME);
            lenient().doReturn(ERIC_IDENTITY_HEADER_VALUE).when(request).getHeader(ERIC_IDENTITY_TYPE_HEADER_NAME);

            assertFalse(userAuthenticationInterceptor.preHandle(request, response, null));
        }
        @Test
        @DisplayName("Authentication : ERIC-Identity-Type header MISSING")
        void missingEricIdentityTypeHeader() {

            lenient().doReturn(ERIC_IDENTITY_HEADER_VALUE).when(request).getHeader(ERIC_IDENTITY_HEADER_NAME);
            lenient().doReturn(null).when(request).getHeader(ERIC_IDENTITY_TYPE_HEADER_NAME);

            assertFalse(userAuthenticationInterceptor.preHandle(request, response, null));
        }
        @Test
        @DisplayName("Authentication : ERIC headers MISSING")
        void missingBothEricHeaders() {

            lenient().doReturn(null).when(request).getHeader(ERIC_IDENTITY_HEADER_NAME);
            lenient().doReturn(null).when(request).getHeader(ERIC_IDENTITY_TYPE_HEADER_NAME);

            assertFalse(userAuthenticationInterceptor.preHandle(request, response, null));
        }
    }

    @Test
    @DisplayName("Authentication : ERIC-Identity-Type ERIC-Identity present")
    void bothEricHeadersPresent() {
        lenient().doReturn(ERIC_IDENTITY_HEADER_VALUE).when(request).getHeader(ERIC_IDENTITY_HEADER_NAME);
        lenient().doReturn(ERIC_IDENTITY_HEADER_VALUE).when(request).getHeader(ERIC_IDENTITY_TYPE_HEADER_NAME);

        assertTrue(userAuthenticationInterceptor.preHandle(request, response, null));
    }
}