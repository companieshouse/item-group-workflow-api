package uk.gov.companieshouse.itemgroupworkflowapi.interceptor;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.Constants.REQUEST_ID_HEADER_NAME;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.companieshouse.api.util.security.AuthorisationUtil;
import uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtils;
import uk.gov.companieshouse.logging.util.DataMap;

@Component
public class UserAuthorisationInterceptor implements HandlerInterceptor {
    private final LoggingUtils logger;

    public UserAuthorisationInterceptor(LoggingUtils logger) {
        this.logger = logger;
    }

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
        if(AuthorisationUtil.hasInternalUserRole(request)) {
            var dataMap = new DataMap.Builder()
                    .requestId(request.getHeader(REQUEST_ID_HEADER_NAME))
                    .build();
            logger.getLogger().trace("API is permitted to view the resource",
                    dataMap.getLogMap());
            return true;
        } else {
            var dataMap = new DataMap.Builder()
                    .requestId(request.getHeader(REQUEST_ID_HEADER_NAME))
                    .status(UNAUTHORIZED.toString())
                    .build();
            logger.getLogger().info("API is NOT permitted to perform a " + request.getMethod(),
                    dataMap.getLogMap());
            return false;
        }
    }
}