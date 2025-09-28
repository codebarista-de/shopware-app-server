package de.codebarista.shopware.appserver.service;

import de.codebarista.shopware.appserver.config.AppServerProperties;
import de.codebarista.shopware.appserver.exception.ShopwareAccessException;
import de.codebarista.shopware.appserver.model.ShopwareShopEntity;
import de.codebarista.shopware.appserver.service.dto.auth.ShopwareAdminApiAccessTokenDto;
import de.codebarista.shopware.appserver.service.dto.auth.ShopwareAdminApiTokenRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ShopwareAccessTokenClientService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShopwareAccessTokenClientService.class);

    private final RestTemplate restTemplate;
    private final ShopManagementService shopManagementService;
    private final ConcurrentHashMap<CacheKey, CachedToken> tokenCache;
    private final boolean sslOnly;

    public ShopwareAccessTokenClientService(RestTemplate restTemplate,
                                            ShopManagementService shopManagementService,
                                            AppServerProperties appServerProperties) {
        this.restTemplate = restTemplate;
        this.shopManagementService = shopManagementService;
        tokenCache = new ConcurrentHashMap<>();
        sslOnly = appServerProperties.isSslOnly();
    }

    public String getAccessToken(ShopwareApp app, String shopId) {
        ShopwareShopEntity shop = shopManagementService.getShopByIdOrThrow(app, shopId);
        CachedToken cachedToken = tokenCache.get(new CacheKey(app, shopId));
        if (cachedToken != null && isTokenValid(cachedToken, shop)) {
            LOGGER.debug("Return shopware access token from cache for {}: {}", app, shopId);
            return cachedToken.tokenDto.getAccessToken();
        }

        LOGGER.debug("Request new shopware access token for {}: {}", app, shopId);
        ShopwareAdminApiAccessTokenDto newToken = requestNewAccessToken(shop);
        cacheToken(app, shopId, newToken);
        return newToken.getAccessToken();
    }

    private ShopwareAdminApiAccessTokenDto requestNewAccessToken(ShopwareShopEntity shop) {
        var headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        var accessTokenRequest = new ShopwareAdminApiTokenRequest(
                shop.getShopAdminApiKey(), shop.getShopAdminApiSecretKey());

        var requestEntity = new HttpEntity<>(accessTokenRequest, headers);

        var urlBuilder = UriComponentsBuilder.fromHttpUrl(shop.getShopRequestUrl());
        if (sslOnly) {
            urlBuilder.scheme("https");
        }
        var url = urlBuilder
                .pathSegment("api", "oauth", "token")
                .encode()
                .build()
                .toUri();

        try {
            var response = restTemplate.exchange(
                    url, HttpMethod.POST, requestEntity, ShopwareAdminApiAccessTokenDto.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                var accessToken = response.getBody();
                if (accessToken != null && accessToken.getAccessToken() != null) {
                    return response.getBody();
                } else {
                    LOGGER.error("POST {} failed with unexpected body {}", url, accessToken);
                }
            } else {
                LOGGER.error("POST {} failed with status {}", url, response.getStatusCode().value());
            }
            throw ShopwareAccessException.create(shop.getShopId());

        } catch (RestClientException e) {
            LOGGER.error("POST {} failed", url, e);
            throw ShopwareAccessException.create(shop.getShopId());
        }
    }

    private void cacheToken(ShopwareApp app, String shopId, ShopwareAdminApiAccessTokenDto token) {
        // the expires_in value from the token is the "time to live" of the token from the moment of its creation.
        // we do not fully exhaust this time. After 3/4 of the ttl passed, we request a fresh token.
        long addedMillis = (long) (token.getExpiresIn() * 1000L * 0.75);
        long expiresAt = System.currentTimeMillis() + addedMillis;
        tokenCache.put(new CacheKey(app, shopId), new CachedToken(token, OffsetDateTime.now(), expiresAt));
        LOGGER.debug("Added new shopware access token to cache for {} {}", app, shopId);
    }

    private boolean isTokenValid(CachedToken token, ShopwareShopEntity shop) {
        return token.expiresAt > System.currentTimeMillis() && shop.getRegistrationConfirmedAt().isBefore(token.createdAt);
    }

    private static class CacheKey {
        ShopwareApp app;
        String shopId;

        public CacheKey(ShopwareApp app, String shopId) {
            this.app = app;
            this.shopId = shopId;
        }

        @Override
        public final boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CacheKey cacheKey)) return false;

            return Objects.equals(app.getAppKey(), cacheKey.app.getAppKey()) && Objects.equals(shopId, cacheKey.shopId);
        }

        @Override
        public int hashCode() {
            int result = Objects.hashCode(app.getAppKey());
            result = 31 * result + Objects.hashCode(shopId);
            return result;
        }
    }

    private record CachedToken(ShopwareAdminApiAccessTokenDto tokenDto, OffsetDateTime createdAt, long expiresAt) {
    }

}
