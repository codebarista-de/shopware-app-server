package de.codebarista.shopware.appserver.service;

import de.codebarista.shopware.appserver.config.AppServerProperties;
import de.codebarista.shopware.appserver.model.ShopwareShopEntity;
import de.codebarista.shopware.appserver.service.dto.CreateShopwareNotificationDto;
import de.codebarista.shopware.appserver.service.dto.search.SearchQuery;
import de.codebarista.shopware.appserver.service.dto.search.SearchResult;
import de.codebarista.shopware.appserver.service.dto.sync.SyncResult;
import jakarta.annotation.Nonnull;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminApiService implements AdminApi {
    public static final String BEARER_PREFIX = "Bearer ";
    private final RestTemplate restTemplate;
    private final ShopManagementService shopManagementService;
    private final ShopwareAccessTokenClientService shopwareAccessTokenClientService;
    private final boolean sslOnly;
    private int pageSize = 100;

    public AdminApiService(RestTemplate restTemplate,
                           ShopManagementService shopManagementService,
                           ShopwareAccessTokenClientService shopwareAccessTokenClientService,
                           AppServerProperties appServerProperties) {
        this.restTemplate = restTemplate;
        restTemplate.setInterceptors(List.of(new HttpRequestResponseLoggingInterceptor()));
        this.shopManagementService = shopManagementService;
        this.shopwareAccessTokenClientService = shopwareAccessTokenClientService;
        sslOnly = appServerProperties.isSslOnly();
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public UriComponentsBuilder getShopUrlBuilder(ShopwareApp app, String shopId) {
        ShopwareShopEntity shop = shopManagementService.getShopByIdOrThrow(app, shopId);
        var builder = UriComponentsBuilder.fromUriString(shop.getShopRequestUrl());
        if (sslOnly) {
            builder.scheme("https");
        }
        return builder;
    }

    public <T> @Nonnull List<T> search(ShopwareApp app,
                                       String shopId,
                                       String entityName,
                                       SearchQuery query,
                                       Class<? extends SearchResult<T>> responseType,
                                       String shopwareLanguageId) {
        List<T> results = new ArrayList<>();
        query.limit(pageSize);
        for (int currentPageNum = 1; ; currentPageNum++) {
            query.page(currentPageNum);
            SearchResult<T> page = postSearch(app, shopId, query, entityName, responseType, shopwareLanguageId);
            results.addAll(page.data());
            if (page.data().isEmpty() || page.total() < pageSize) {
                break;
            }
        }
        return results;
    }

    public <T> SearchResult<T> postSearch(ShopwareApp app,
                                          String shopId,
                                          SearchQuery searchQuery,
                                          String entityName,
                                          Class<? extends SearchResult<T>> responseType,
                                          String shopwareLanguageId) {
        var headers = getTokenAndSetAuthorizationHeader(app, shopId);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        // set this header to get a response from Shopware where all data is included in top DTO
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        if (shopwareLanguageId != null) {
            headers.add("sw-language-id", shopwareLanguageId);
        }

        // Replace underscores with dashes to prevent common API lookup failures.
        // When developers copy entity names from Shopware code (which uses
        // underscore_naming), those names won't work with the admin API that
        // expects dash-naming. This replacement allows both formats to work,
        // avoiding the frustrating debugging cycle of "entity not found" errors
        // when using underscored names copied directly from Definition classes.
        entityName = entityName.replace('_', '-');

        URI url = getShopUrlBuilder(app, shopId).pathSegment("api", "search", entityName).build().toUri();
        return postForObject(url, new HttpEntity<>(searchQuery, headers), responseType);
    }


    public <T> @Nonnull T getForObject(URI url, HttpEntity<?> request, Class<T> responseType) {
        var response = restTemplate.exchange(url, HttpMethod.GET, request, responseType);
        var object = response.getBody();
        if (object == null) {
            throw new RestClientException(String.format("Missing response body on GET request for \"%s\": %s ", url, request));
        }
        return object;
    }

    @Override
    public void pushSuccessMessage(ShopwareApp app, String shopId, String message) {
        pushMessage(app, shopId, CreateShopwareNotificationDto.success(message));
    }

    @Override
    public void pushInfoMessage(ShopwareApp app, String shopId, String message) {
        pushMessage(app, shopId, CreateShopwareNotificationDto.info(message));
    }

    @Override
    public void pushWarningMessage(ShopwareApp app, String shopId, String message) {
        pushMessage(app, shopId, CreateShopwareNotificationDto.warning(message));
    }

    @Override
    public void pushErrorMessage(ShopwareApp app, String shopId, String message) {
        pushMessage(app, shopId, CreateShopwareNotificationDto.error(message));
    }

    private void pushMessage(ShopwareApp app, String shopId, CreateShopwareNotificationDto notification) {
        postEntity(app, shopId, "notification", notification);
    }

    public <T> void postEntity(ShopwareApp app, String shopId, String entity, T requestDto) {
        URI url = getShopUrlBuilder(app, shopId).pathSegment("api", entity).build().toUri();
        var request = new HttpEntity<>(requestDto, getTokenAndSetAuthorizationHeader(app, shopId));
        restTemplate.postForObject(url, request, Object.class);
    }

    @Override
    public <T> @Nonnull T script(ShopwareApp app, String shopId, String hookName, Object requestBody, Class<T> responseClass) {
        var url = getShopUrlBuilder(app, shopId).pathSegment("api", "script", hookName).build().toUri();
        var headers = getTokenAndSetAuthorizationHeader(app, shopId);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return postForObject(url, new HttpEntity<>(requestBody, headers), responseClass);
    }

    @Override
    public @Nonnull SyncResult sync(ShopwareApp app, String shopId, Object requestBody) {
        return sync(app, shopId, requestBody, SyncResult.class);
    }

    @Override
    public <T> @Nonnull T sync(ShopwareApp app, String shopId, Object requestBody, Class<T> responseClass) {
        var url = getShopUrlBuilder(app, shopId).pathSegment("api", "_action", "sync").build().toUri();
        var headers = getTokenAndSetAuthorizationHeader(app, shopId);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add("single-operation", "1");
        headers.add("indexing-behavior", "use-queue-indexing");
        return postForObject(url, new HttpEntity<>(requestBody, headers), responseClass);
    }

    public <T> @Nonnull T postForObject(URI url, HttpEntity<?> request, Class<T> responseClass) {
        T rsp = restTemplate.postForObject(url, request, responseClass);
        if (rsp == null) {
            throw new RestClientException(String.format("Missing response body on POST request for %s: %s", url, request));
        }
        return rsp;
    }

    public HttpHeaders getTokenAndSetAuthorizationHeader(ShopwareApp app, String shopId) {
        String accessToken = shopwareAccessTokenClientService.getAccessToken(app, shopId);
        var headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken);
        return headers;
    }
}
