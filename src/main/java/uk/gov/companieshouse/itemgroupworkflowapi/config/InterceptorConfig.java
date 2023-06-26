package uk.gov.companieshouse.itemgroupworkflowapi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.itemgroupworkflowapi.interceptor.UserAuthenticationInterceptor;
import uk.gov.companieshouse.itemgroupworkflowapi.interceptor.UserAuthorisationInterceptor;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    @Autowired
    UserAuthorisationInterceptor userAuthenticationInterceptor;
    @Autowired
    UserAuthenticationInterceptor userAuthorisationInterceptor;

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(userAuthenticationInterceptor);
        registry.addInterceptor(userAuthorisationInterceptor);
    }
}
