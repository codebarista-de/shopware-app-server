package de.codebarista.shopware.appbackend.sdk.service;

import de.codebarista.shopware.appbackend.sdk.service.dto.search.SearchQuery;
import de.codebarista.shopware.appbackend.sdk.service.dto.search.SearchResult;
import de.codebarista.shopware.appbackend.sdk.service.dto.sync.SyncResult;
import jakarta.annotation.Nonnull;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

public interface AdminApi {
    void pushSuccessMessage(ShopwareApp app, String shopId, String message);

    void pushInfoMessage(ShopwareApp app, String shopId, String message);

    void pushWarningMessage(ShopwareApp app, String shopId, String message);

    void pushErrorMessage(ShopwareApp app, String shopId, String message);

    /**
     * Searches all instances of an entity that match the query
     *
     * @param app                the app that implements the endpoint
     * @param shopId             the shop whose admin api is called
     * @param entityName         the name of the entity whose instances are searched.
     * @param query              the search criteria
     * @param responseType       java type of the search result
     * @param shopwareLanguageId ID of the language in which the Admin-API is queried. If an entity has translated fields,
     *                           they will be populated with the values of this language.
     *                           You can use TranslatedField from the Shopware Model project
     *                           (de.codebarista.shopware.model.extension.TranslatedField) to get the translated
     *                           property value.
     * @param <T>                java type of the entity
     * @return a list of all entity instances that match the query
     */
    <T> @Nonnull List<T> search(ShopwareApp app, String shopId, String entityName, SearchQuery query, Class<? extends SearchResult<T>> responseType, String shopwareLanguageId);

    /**
     * Like {@link AdminApi#search(ShopwareApp, String, String, SearchQuery, Class, String)} but {@code shopwareLanguageId} set to {@code null}
     */
    default <T> @Nonnull List<T> search(ShopwareApp app, String shopId, String entityName, SearchQuery query, Class<? extends SearchResult<T>> responseType) {
        return search(app, shopId, entityName, query, responseType, null);
    }

    /**
     * Calls an Admin API endpoint implemented by a custom endpoint app script
     * <p>
     * <a href="https://developer.shopware.com/docs/guides/plugins/apps/app-scripts/custom-endpoints.html#admin-api-endpoints">See shopware developer documentation</a>
     *
     * @param app           the app that implements the endpoint
     * @param shopId        the shop whose admin api is called
     * @param hookName      the name of the folder that contains the app script without the 'app-' prefix
     * @param requestBody   object that will be serialized into the json body of the request
     * @param responseClass class in which the json response will be deserialized
     * @param <T>           type of the response class
     * @return deserialized json response
     */
    <T> @Nonnull T script(ShopwareApp app, String shopId, String hookName, Object requestBody, Class<T> responseClass);

    @Nonnull
    SyncResult sync(ShopwareApp app, String shopId, Object requestBody);

    <T> @Nonnull T sync(ShopwareApp app, String shopId, Object requestBody, Class<T> responseClass);

    UriComponentsBuilder getShopUrlBuilder(ShopwareApp app, String shopId);
}
