package de.codebarista.shopware.appserver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the Shopware App Server.
 */
@ConfigurationProperties(prefix = "app-server")
public class AppServerProperties {

    /**
     * Enable HTTP request/response logging for debugging purposes.
     * When enabled, all HTTP requests to Shopware APIs will be logged.
     */
    private boolean httpRequestResponseLoggingEnabled = false;

    /**
     * Map localhost IP addresses to localhost domain name in URLs.
     * Useful for development when Shopware expects domain names instead of IP addresses.
     */
    private boolean mapLocalhostIPToLocalhostDomainName = false;

    /**
     * Enforce SSL-only communication with Shopware.
     * When enabled, all HTTP URLs will be rejected or converted to HTTPS.
     */
    private boolean sslOnly = true;

    /**
     * Enforce shop signature verification on re-registration.
     * When enabled, all re-registration requests for confirmed shops must include
     * a valid {@code shopware-shop-signature} header.
     */
    private boolean enforceReRegistrationWithShopSignature = false;

    public boolean isHttpRequestResponseLoggingEnabled() {
        return httpRequestResponseLoggingEnabled;
    }

    public void setHttpRequestResponseLoggingEnabled(boolean loggingEnabled) {
        this.httpRequestResponseLoggingEnabled = loggingEnabled;
    }

    /**
     * Gets the {@link #mapLocalhostIPToLocalhostDomainName}.
     */
    public boolean isMapLocalhostIPToLocalhostDomainName() {
        return mapLocalhostIPToLocalhostDomainName;
    }

    /**
     * Sets the {@link #mapLocalhostIPToLocalhostDomainName}
     */
    public void setMapLocalhostIPToLocalhostDomainName(boolean mapLocalhostIPToLocalhostDomainName) {
        this.mapLocalhostIPToLocalhostDomainName = mapLocalhostIPToLocalhostDomainName;
    }

    /**
     * Gets the {@link #sslOnly}.
     */
    public boolean isSslOnly() {
        return sslOnly;
    }

    /**
     * Sets the {@link #sslOnly}
     */
    public void setSslOnly(boolean sslOnly) {
        this.sslOnly = sslOnly;
    }

    public boolean isReRegistrationWithShopSignatureEnforced() {
        return enforceReRegistrationWithShopSignature;
    }

    public void setEnforceReRegistrationWithShopSignature(boolean enforceReRegistrationWithShopSignature) {
        this.enforceReRegistrationWithShopSignature = enforceReRegistrationWithShopSignature;
    }
}
