package uk.gov.companieshouse.itemgroupworkflowapi.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtilsConfiguration;
import uk.gov.companieshouse.itemgroupworkflowapi.util.EricHeaderHelper;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtilsConfiguration.*;

@Component
public class UserAuthenticationInterceptor implements HandlerInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingUtilsConfiguration.APPLICATION_NAMESPACE);

    /**
     * @return true if ERIC-Identity-Type and ERIC-Identity headers exist and have a value
     * or false and sets response status to UNAUTHORIZED
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(REQUEST_ID_LOG_KEY, request.getHeader(REQUEST_ID_HEADER_NAME));

        String identityType = EricHeaderHelper.getIdentityType(request);
        if(identityType == null) {
            logMap.put(IDENTITY_TYPE_LOG_KEY, MISSING_REQUIRED_INFO);
            logMap.put(STATUS_LOG_KEY, UNAUTHORIZED);
            LOGGER.infoRequest(request, "UserAuthenticationInterceptor error: no ERIC-Identity-Type", logMap);
            response.setStatus(UNAUTHORIZED.value());
            return false;  // NOT Authorised.
        }

        String identity = EricHeaderHelper.getIdentity(request);
        if(identity == null) {
            logMap.put(IDENTITY_LOG_KEY, MISSING_REQUIRED_INFO);
            logMap.put(STATUS_LOG_KEY, UNAUTHORIZED);
            LOGGER.infoRequest(request, "UserAuthenticationInterceptor error: no ERIC-Identity", logMap);
            response.setStatus(UNAUTHORIZED.value());
            return false;  // NOT Authorised.
        }
        return true;   // Authorised OK...
    }
}