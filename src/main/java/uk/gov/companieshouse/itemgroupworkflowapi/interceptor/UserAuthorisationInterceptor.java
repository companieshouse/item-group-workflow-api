package uk.gov.companieshouse.itemgroupworkflowapi.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.companieshouse.api.util.security.AuthorisationUtil;
import uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtilsConfiguration;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtilsConfiguration.*;

@Component
public class UserAuthorisationInterceptor implements HandlerInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingUtilsConfiguration.APPLICATION_NAMESPACE);
    /**
     * @return true if ERIC-Authorised-Key-Roles="*" or false and sets response status to UNAUTHORIZED
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(hasInternalUserRole(request))
            return true;

        response.setStatus(UNAUTHORIZED.value());
        return false;
    }

    private boolean hasInternalUserRole(HttpServletRequest request) {
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(REQUEST_ID_LOG_KEY, request.getHeader(REQUEST_ID_HEADER_NAME));

        if(AuthorisationUtil.hasInternalUserRole(request)) {
            LOGGER.trace("API is permitted to view the resource", logMap);
            return true;
        } else {
            logMap.put(STATUS_LOG_KEY, UNAUTHORIZED);
            LOGGER.error("API is NOT permitted to perform a " + request.getMethod(), logMap);
            return false;
        }
    }
}