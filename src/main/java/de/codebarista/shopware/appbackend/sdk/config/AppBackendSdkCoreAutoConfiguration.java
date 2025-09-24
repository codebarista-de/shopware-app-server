package de.codebarista.shopware.appbackend.sdk.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Core auto-configuration for Shopware App Backend SDK.
 * Handles general SDK configuration like SSL enforcement and localhost domain mapping.
 */
@AutoConfiguration
@EnableConfigurationProperties(AppBackendSdkProperties.class)
public class AppBackendSdkCoreAutoConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppBackendSdkCoreAutoConfiguration.class);

    /**
     * Configuration bean that provides SDK-wide settings.
     * This replaces the previous AppBackendSdkConfig wrapper class.
     */
    @Bean
    public AppBackendSdkConfiguration sdkConfiguration(AppBackendSdkProperties properties) {
        LOGGER.debug("Configuring Shopware App Backend SDK with SSL-only: {}, localhost mapping: {}",
                properties.isSslOnly(), properties.isMapLocalhostIPToLocalhostDomainName());

        return AppBackendSdkConfiguration.builder()
                .sslOnly(properties.isSslOnly())
                .mapLocalhostIPToLocalhostDomainName(properties.isMapLocalhostIPToLocalhostDomainName())
                .httpRequestResponseLoggingEnabled(properties.isHttpRequestResponseLoggingEnabled())
                .build();
    }

    /**
     * Nested configuration class that holds SDK settings.
     * Uses builder pattern for clean construction.
     */
    public static class AppBackendSdkConfiguration {
        private final boolean sslOnly;
        private final boolean mapLocalhostIPToLocalhostDomainName;
        private final boolean httpRequestResponseLoggingEnabled;

        private AppBackendSdkConfiguration(Builder builder) {
            this.sslOnly = builder.sslOnly;
            this.mapLocalhostIPToLocalhostDomainName = builder.mapLocalhostIPToLocalhostDomainName;
            this.httpRequestResponseLoggingEnabled = builder.httpRequestResponseLoggingEnabled;
        }

        public static Builder builder() {
            return new Builder();
        }

        public boolean isSslOnly() {
            return sslOnly;
        }

        public boolean isMapLocalhostIPToLocalhostDomainName() {
            return mapLocalhostIPToLocalhostDomainName;
        }

        public boolean isHttpRequestResponseLoggingEnabled() {
            return httpRequestResponseLoggingEnabled;
        }

        public static class Builder {
            private boolean sslOnly = true;
            private boolean mapLocalhostIPToLocalhostDomainName = false;
            private boolean httpRequestResponseLoggingEnabled = false;

            public Builder sslOnly(boolean sslOnly) {
                this.sslOnly = sslOnly;
                return this;
            }

            public Builder mapLocalhostIPToLocalhostDomainName(boolean mapLocalhostIPToLocalhostDomainName) {
                this.mapLocalhostIPToLocalhostDomainName = mapLocalhostIPToLocalhostDomainName;
                return this;
            }

            public Builder httpRequestResponseLoggingEnabled(boolean httpRequestResponseLoggingEnabled) {
                this.httpRequestResponseLoggingEnabled = httpRequestResponseLoggingEnabled;
                return this;
            }

            public AppBackendSdkConfiguration build() {
                return new AppBackendSdkConfiguration(this);
            }
        }
    }
}