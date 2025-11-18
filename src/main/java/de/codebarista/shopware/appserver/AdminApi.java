package de.codebarista.shopware.appserver;

import de.codebarista.shopware.appserver.service.dto.search.SearchQuery;
import de.codebarista.shopware.appserver.service.dto.search.SearchResult;
import de.codebarista.shopware.appserver.service.dto.sync.SyncResult;
import jakarta.annotation.Nonnull;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

/**
 * Service interface for interacting with the Shopware Admin API.
 * <p>
 * This interface provides methods to interact with Shopware shops via their Admin API,
 * including sending notifications, searching entities, posting data, and executing sync operations.
 * All operations are authenticated using OAuth tokens managed by the App Server.
 */
public interface AdminApi {
    /**
     * Pushes a success notification to the Shopware Administration UI.
     *
     * @param app     the app from which the notification is sent
     * @param shopId  the Shopware shop ID to receive the notification
     * @param message the message text to display
     */
    void pushSuccessMessage(ShopwareApp app, String shopId, String message);

    /**
     * Pushes an info notification to the Shopware Administration UI.
     *
     * @param app     the app from which the notification is sent
     * @param shopId  the Shopware shop ID to receive the notification
     * @param message the message text to display
     */
    void pushInfoMessage(ShopwareApp app, String shopId, String message);

    /**
     * Pushes a warning notification to the Shopware Administration UI.
     *
     * @param app     the app from which the notification is sent
     * @param shopId  the Shopware shop ID to receive the notification
     * @param message the message text to display
     */
    void pushWarningMessage(ShopwareApp app, String shopId, String message);

    /**
     * Pushes an error notification to the Shopware Administration UI.
     *
     * @param app     the app from which the notification is sent
     * @param shopId  the Shopware shop ID to receive the notification
     * @param message the message text to display
     */
    void pushErrorMessage(ShopwareApp app, String shopId, String message);

    /**
     * Posts (creates) a new entity instance to the Shopware Admin API.
     *
     * @param app        the app making the request
     * @param shopId     the Shopware shop ID
     * @param entity     the entity name (e.g., "product", "order")
     * @param requestDto the entity data to post
     * @param <T>        the type of the request DTO
     */
    <T> void postEntity(ShopwareApp app, String shopId, String entity, T requestDto);

    // TODO: also expose a postObject(Shopware app, String, shop Id, URI url, data, headers) to post e.g. PDF-Documents

    //TODO: also expose a getObject(ShopwareApp app, String shopId, url-part, Class<T> responseType)

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

    /**
     * Executes a sync operation against the Shopware Admin API.
     * <p>
     * The Sync API allows for bulk operations (create, update, delete) across multiple entities in a single request.
     *
     * @param app         the app making the request
     * @param shopId      the Shopware shop ID
     * @param requestBody the sync payload containing operations to perform
     * @return the sync result indicating success or failure
     */
    @Nonnull
    SyncResult sync(ShopwareApp app, String shopId, Object requestBody);

    /**
     * Executes a sync operation against the Shopware Admin API and deserializes the response.
     * <p>
     * The Sync API allows for bulk operations (create, update, delete) across multiple entities in a single request.
     *
     * @param app           the app making the request
     * @param shopId        the Shopware shop ID
     * @param requestBody   the sync payload containing operations to perform
     * @param responseClass the class to deserialize the response into
     * @param <T>           the type of the response
     * @return the deserialized sync response
     */
    <T> @Nonnull T sync(ShopwareApp app, String shopId, Object requestBody, Class<T> responseClass);

    /**
     * Creates a UriComponentsBuilder pre-configured with the shop's base URL.
     * <p>
     * Use this to construct custom URLs for accessing Shopware resources.
     *
     * @param app    the app making the request
     * @param shopId the Shopware shop ID
     * @return a UriComponentsBuilder initialized with the shop's URL
     */
    UriComponentsBuilder getShopUrlBuilder(ShopwareApp app, String shopId);
}
