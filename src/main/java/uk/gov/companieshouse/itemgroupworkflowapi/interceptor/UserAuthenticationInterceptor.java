package uk.gov.companieshouse.itemgroupworkflowapi.interceptor;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static uk.gov.companieshouse.itemgroupworkflowapi.util.Constants.REQUEST_ID_HEADER_NAME;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtils;
import uk.gov.companieshouse.itemgroupworkflowapi.util.EricHeaderHelper;
import uk.gov.companieshouse.logging.util.DataMap;

@Component
public class UserAuthenticationInterceptor implements HandlerInterceptor {
    private final LoggingUtils logger;

    public UserAuthenticationInterceptor(LoggingUtils logger) {
        this.logger = logger;
    }

    /**
     * @return true if ERIC-Identity-Type and ERIC-Identity headers exist and have a value
     * or false and sets response status to UNAUTHORIZED
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        String identityType = EricHeaderHelper.getIdentityType(request);
        if(identityType == null) {
            var dataMap = new DataMap.Builder()
                    .requestId(request.getHeader(REQUEST_ID_HEADER_NAME))
                    .status(UNAUTHORIZED.toString())
                    .build();

            var errorMessage = "UserAuthenticationInterceptor error: no ERIC-Identity-Type header";
            logFailureToAuthenticate(errorMessage, dataMap);
            response.setStatus(UNAUTHORIZED.value());
            return false;  // NOT Authorised.
        }

        String identity = EricHeaderHelper.getIdentity(request);
        if(identity == null) {
            var dataMap = new DataMap.Builder()
                    .requestId(request.getHeader(REQUEST_ID_HEADER_NAME))
                    .status(UNAUTHORIZED.toString())
                    .build();
            var errorMessage = "UserAuthenticationInterceptor error: no ERIC-Identity header";

            logFailureToAuthenticate(errorMessage, dataMap);
            response.setStatus(UNAUTHORIZED.value());
            return false;  // NOT Authorised.
        }
        return true;   // Authorised OK...
    }

    private void logFailureToAuthenticate(String errorMessage, DataMap dataMap){
        logger.logger().info(errorMessage,
                dataMap.getLogMap());
    }
}