package uk.gov.companieshouse.itemgroupworkflowapi.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtils;
import uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtilsConfiguration;
import uk.gov.companieshouse.itemgroupworkflowapi.util.EricHeaderHelper;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.logging.util.DataMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtilsConfiguration.*;

@Component
public class UserAuthenticationInterceptor implements HandlerInterceptor {
    @Autowired
    private final LoggingUtils logger;

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingUtilsConfiguration.APPLICATION_NAMESPACE);

    public UserAuthenticationInterceptor(LoggingUtils logger) {
        this.logger = logger;
    }

    /**
     * @return true if ERIC-Identity-Type and ERIC-Identity headers exist and have a value
     * or false and sets response status to UNAUTHORIZED
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        DataMap dataMap = new DataMap.Builder()
                .requestId(request.getHeader(REQUEST_ID_HEADER_NAME))
                .status(UNAUTHORIZED.toString())
                .build();

        String identityType = EricHeaderHelper.getIdentityType(request);
        if(identityType == null) {
            String errorMessage = "UserAuthenticationInterceptor error: no ERIC-Identity-Type header";
            logFailureToAuthenticate(errorMessage, dataMap);
            response.setStatus(UNAUTHORIZED.value());
            return false;  // NOT Authorised.
        }

        String identity = EricHeaderHelper.getIdentity(request);
        if(identity == null) {
            String errorMessage = "UserAuthenticationInterceptor error: no ERIC-Identity header";
            logFailureToAuthenticate(errorMessage, dataMap);
            response.setStatus(UNAUTHORIZED.value());
            return false;  // NOT Authorised.
        }
        return true;   // Authorised OK...
    }

    private void logFailureToAuthenticate(String errorMessage, DataMap dataMap){
        logger.getLogger().info("UserAuthenticationInterceptor error: no ERIC-Identity-Type header",
                dataMap.getLogMap());
    }
}