package de.codebarista.shopware.appserver.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.codebarista.shopware.appserver.ShopwareApp;
import de.codebarista.shopware.appserver.TokenService;
import de.codebarista.shopware.appserver.model.ShopwareShopEntityRepository;
import de.codebarista.shopware.appserver.service.AdminApiService;
import de.codebarista.shopware.appserver.service.AppLookupService;
import de.codebarista.shopware.appserver.service.ShopManagementService;
import de.codebarista.shopware.appserver.service.ShopwareAccessTokenClientService;
import de.codebarista.shopware.appserver.service.SignatureService;
import de.codebarista.shopware.appserver.service.TokenServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Auto-configuration for Shopware App Server core services.
 * Provides all essential services needed for Shopware app integration.
 * <p>
 * All beans are created with {@link ConditionalOnMissingBean}, allowing users
 * to override any service by defining their own bean of the same type.
 */
@AutoConfiguration(after = {AppServerHttpAutoConfiguration.class, AppServerComponentAutoConfiguration.class})
@EnableConfigurationProperties(AppServerProperties.class)
public class AppServerServiceAutoConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppServerServiceAutoConfiguration.class);

    /**
     * Service for cryptographic signature calculation and verification.
     * Handles HMAC-SHA256 signatures and SHA-256 hashing.
     * <p>
     * Users can override by defining their own {@code SignatureService} bean.
     *
     * @param objectMapper Jackson ObjectMapper for JSON serialization
     * @return Configured SignatureService
     */
    @Bean
    @ConditionalOnMissingBean
    public SignatureService signatureService(ObjectMapper objectMapper) {
        LOGGER.debug("Configuring SignatureService");
        return new SignatureService(objectMapper);
    }

    /**
     * Service for looking up ShopwareApp instances by key or host.
     * Automatically collects all ShopwareApp beans from the application context.
     * <p>
     * Users can override by defining their own {@code AppLookupService} bean.
     *
     * @param apps All ShopwareApp beans available in the application context
     * @return Configured AppLookupService
     */
    @Bean
    @ConditionalOnMissingBean
    public AppLookupService appLookupService(List<ShopwareApp> apps) {
        LOGGER.debug("Configuring AppLookupService with {} app(s)", apps.size());
        return new AppLookupService(apps);
    }

    /**
     * Service for managing shop registrations and lifecycle.
     * Handles shop registration, confirmation, and deletion.
     * <p>
     * Users can override by defining their own {@code ShopManagementService} bean.
     *
     * @param shopwareShopEntityRepository Repository for shop data persistence
     * @param properties App server configuration properties
     * @return Configured ShopManagementService
     */
    @Bean
    @ConditionalOnMissingBean
    public ShopManagementService shopManagementService(
            ShopwareShopEntityRepository shopwareShopEntityRepository,
            AppServerProperties properties) {
        LOGGER.debug("Configuring ShopManagementService");
        return new ShopManagementService(shopwareShopEntityRepository, properties);
    }

    /**
     * Service for obtaining and caching Shopware Admin API access tokens.
     * Implements token caching to reduce API calls and improve performance.
     * <p>
     * Users can override by defining their own {@code ShopwareAccessTokenClientService} bean.
     *
     * @param shopwareRestTemplate RestTemplate configured for Shopware API calls
     * @param shopManagementService Service for accessing shop data
     * @param properties App server configuration properties
     * @return Configured ShopwareAccessTokenClientService
     */
    @Bean
    @ConditionalOnMissingBean
    public ShopwareAccessTokenClientService shopwareAccessTokenClientService(
            RestTemplate shopwareRestTemplate,
            ShopManagementService shopManagementService,
            AppServerProperties properties) {
        LOGGER.debug("Configuring ShopwareAccessTokenClientService");
        return new ShopwareAccessTokenClientService(shopwareRestTemplate, shopManagementService, properties);
    }

    /**
     * Service providing high-level Shopware Admin API operations.
     * Offers convenient methods for searching entities, sending notifications,
     * executing app scripts, and performing sync operations.
     * <p>
     * Users can override by defining their own {@code AdminApiService} bean.
     *
     * @param shopwareRestTemplate RestTemplate configured for Shopware API calls
     * @param shopManagementService Service for accessing shop data
     * @param shopwareAccessTokenClientService Service for obtaining access tokens
     * @param properties App server configuration properties
     * @return Configured AdminApiService
     */
    @Bean
    @ConditionalOnMissingBean
    public AdminApiService adminApiService(
            RestTemplate shopwareRestTemplate,
            ShopManagementService shopManagementService,
            ShopwareAccessTokenClientService shopwareAccessTokenClientService,
            AppServerProperties properties) {
        LOGGER.debug("Configuring AdminApiService");
        return new AdminApiService(shopwareRestTemplate, shopManagementService, shopwareAccessTokenClientService, properties);
    }

    /**
     * Service for generating and validating app tokens.
     * Provides token-based authentication for app-to-shop communication.
     * <p>
     * Users can override by defining their own {@code TokenService} bean.
     *
     * @param shopManagementService Service for accessing shop data
     * @param signatureService Service for signature operations
     * @return Configured TokenService implementation
     */
    @Bean("tokenService")
    @ConditionalOnMissingBean(TokenService.class)
    public TokenService tokenService(
            ShopManagementService shopManagementService,
            SignatureService signatureService) {
        LOGGER.debug("Configuring TokenService");
        return new TokenServiceImpl(shopManagementService, signatureService);
    }
}
