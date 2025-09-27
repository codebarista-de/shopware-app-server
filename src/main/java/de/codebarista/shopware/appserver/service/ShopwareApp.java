package de.codebarista.shopware.appserver.service;

import de.codebarista.shopware.appserver.api.dto.action.ActionRequestDto;
import de.codebarista.shopware.appserver.api.dto.action.ActionResponseDto;
import de.codebarista.shopware.appserver.api.dto.event.ShopwareEventDto;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Locale;

public interface ShopwareApp {
    /**
     * The subdomain of the app, which is used to map requests to apps and therefore called app-key.
     * The backend part of a shopware app must always be hosted under a subdomain.
     * The subdomain identifies the app and must be unique.
     * When a request from shopware arrives the value returned by this method is used
     * to identify the correct ShopwareApp implementation to handle the request.
     *
     * @return a subdomain (e.g. "upload-app")
     */
    @Nonnull String getAppKey();

    /**
     * The shared secret that is used by shopware to sign requests to the app backend
     *
     * @return the app secret
     */
    @Nonnull String getAppSecret();

    /**
     * The technical name of the Shopware app
     *
     * @return the app name
     */
    @Nonnull String getAppName();

    /**
     * Version of the app backend.
     * <p>
     * If not null, the version string will be injected into admin extension html pages as a dataset
     * attribute 'version' of the body element.
     * <p>
     * This returns the version of the app backend, not the app version installed in a specific shop.
     * @return the version of the app backend or null.
     */
    @Nullable String getVersion();

    /// The name of the app-folder in the shopware-admin-extension folder which contains the admin extension for this app.
    ///
    /// The shopware-admin-extension folder is located in src/main/resources/public/shopware/admin.
    /// Inside the app-folder one or more version-folders which contain index.html files are expected.
    /// E.g.:
    /// <pre>
    /// src/main/resources/public/shopware/admin/myapp/v1/index.html
    /// </pre>
    /// <p>
    /// The following dataset attributes are injected into the HTML page if a corresponding placeholder is present:
    /// <table>
    ///     <tr>
    ///         <th>Attribute</th>
    ///         <th>Placeholder</th>
    ///         <th>Description</th>
    ///     </tr>
    ///     <tr>
    ///         <td>token</td>
    ///         <td>data-token=""</td>
    ///         <td>
    ///             A token that can be used to authenticate requests against the app backend.
    ///             Tokens can be validated with the [TokenService].
    ///         </td>
    ///     </tr>
    ///     <tr>
    ///         <td>version</td>
    ///         <td>data-version=""</td>
    ///         <td>
    ///             The string returned by [ShopwareApp#getVersion()]. Empty string if the version is null.
    ///         </td>
    ///     </tr>
    /// </table>
    /// All occurrences of a placeholder will be replaced.
    /// <p>
    /// The version dataset attribute should not be confused with the version-folder.
    /// Both serve a different purpose.
    ///
    /// @return a sub-folder name or null if the app does not have an admin extension
    @Nullable String getAdminExtensionFolderName();

    /**
     * Invoked when a new shop is registered with the app backend.
     * Triggered when the app is installed in a shopware instance.
     *
     * @param shopHost       the host name of the shop (usually a domain)
     * @param shopId         the ID of the shop assigned by shopware
     * @param internalShopId the ID which identifies the shop in this app-backend service instance
     */
    void onRegisterShop(@Nonnull String shopHost, @Nonnull String shopId, long internalShopId);

    /**
     * Invoked when a shop that is already registered is registered again.
     * This can happen if the app is not cleanly uninstalled or if the uninstall-request
     * does not reach the app-backend.
     *
     * @param shopHost       the host name of the shop (usually a domain)
     * @param shopId         the ID of the shop assigned by shopware
     * @param internalShopId the ID which identifies the shop in this app-backend service instance
     */
    void onReRegisterShop(@Nonnull String shopHost, @Nonnull String shopId, long internalShopId);

    /**
     * Invoked when a shop is deleted from the app backend.
     * Triggered when the app is uninstalled in a shopware instance.
     *
     * @param shopHost       the host name of the shop (usually a domain)
     * @param shopId         the ID of the shop assigned by shopware
     * @param internalShopId the ID which identifies the shop in this app-backend service instance
     */
    void onDeleteShop(@Nonnull String shopHost, @Nonnull String shopId, long internalShopId);

    void onEvent(@Nonnull ShopwareEventDto event, long internalShopId, @Nullable Locale userLocale, @Nullable String shopwareLanguageId);

    @Nonnull ActionResponseDto<?> onAction(@Nonnull ActionRequestDto action, long internalShopId, @Nullable Locale userLocale, @Nullable String shopwareLanguageId);
}
