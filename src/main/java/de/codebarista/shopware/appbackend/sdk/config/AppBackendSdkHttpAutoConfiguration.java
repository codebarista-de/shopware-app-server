package de.codebarista.shopware.appbackend.sdk.config;

import de.codebarista.shopware.appbackend.sdk.service.HttpRequestResponseLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.lang.NonNull;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpClient;

/**
 * Auto-configuration for HTTP client functionality in the Shopware App Backend SDK.
 * Configures RestTemplate for Shopware API communication with proper error handling and logging.
 */
@AutoConfiguration
@ConditionalOnClass(RestTemplate.class)
@EnableConfigurationProperties(AppBackendSdkProperties.class)
public class AppBackendSdkHttpAutoConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppBackendSdkHttpAutoConfiguration.class);

    /**
     * Creates a RestTemplate specifically configured for Shopware API communication.
     * Features:
     * - Disabled automatic redirects to prevent infinite redirect loops
     * - Buffered request factory to disable chunked encoding (required for some Shopware endpoints)
     * - Custom error handler that treats all non-2xx responses as errors
     * - Optional request/response logging
     */
    @Bean
    @ConditionalOnMissingBean(name = "shopwareRestTemplate")
    public RestTemplate shopwareRestTemplate(RestTemplateBuilder restTemplateBuilder, AppBackendSdkProperties properties) {
        var builder = restTemplateBuilder
                .requestFactory(() -> {
                    // Disable automatic redirect following to prevent infinite redirects
                    var jdkClient = HttpClient.newBuilder()
                            .followRedirects(HttpClient.Redirect.NEVER)
                            .build();
                    var jdkClientFactory = new JdkClientHttpRequestFactory(jdkClient);
                    // Wrap in a buffering request factory to disable chunked transfer encoding which does not work
                    // with some Shopware Admin-API endpoints. The complete request body will be buffered in memory
                    // before the request is sent off.
                    return new BufferingClientHttpRequestFactory(jdkClientFactory);
                })
                // Use error handler that throws an exception for all non 2xx response status codes.
                // The default handler only throws exceptions for 4xx and 5xx but not 1xx and 3xx codes.
                .errorHandler(new DefaultResponseErrorHandler() {
                    @Override
                    protected boolean hasError(@NonNull HttpStatusCode statusCode) {
                        return !statusCode.is2xxSuccessful();
                    }
                });

        if (properties.isHttpRequestResponseLoggingEnabled()) {
            LOGGER.info("Enabling HTTP request/response logging for Shopware API calls");
            builder.interceptors(new HttpRequestResponseLoggingInterceptor());
        } else {
            LOGGER.debug("HTTP request/response logging is disabled");
        }

        return builder.build();
    }
}