package uk.gov.companieshouse.itemgroupworkflowapi.util;

import org.apache.commons.lang.StringUtils;
import jakarta.servlet.http.HttpServletRequest;

public final class EricHeaderHelper {
    public static final String ERIC_IDENTITY = "ERIC-Identity";
    public static final String ERIC_IDENTITY_TYPE = "ERIC-Identity-Type";

    private EricHeaderHelper() { }

    public static String getIdentity(HttpServletRequest request) {
        return getHeader(request, ERIC_IDENTITY);
    }

    public static String getIdentityType(HttpServletRequest request) {
        return getHeader(request, ERIC_IDENTITY_TYPE);
    }

    private static String getHeader(HttpServletRequest request, String headerName) {
        String headerValue = request.getHeader(headerName);
        if (StringUtils.isNotBlank(headerValue)) {
            return headerValue;
        }
        return null;
    }
}