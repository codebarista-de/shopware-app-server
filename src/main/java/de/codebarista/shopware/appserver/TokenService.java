package de.codebarista.shopware.appserver;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * Service for managing authentication tokens for app-to-backend communication.
 * <p>
 * Tokens are used to authenticate requests from Admin Extensions (frontend) to custom
 * app backend endpoints. These tokens are different from the OAuth tokens used for
 * Admin API communication.
 */
public interface TokenService {
    /**
     * Generates a new authentication token for the specified app and shop combination.
     * <p>
     * This token can be injected into Admin Extension HTML pages and used to authenticate
     * requests from the frontend to custom backend endpoints.
     *
     * @param app    the app for which to generate the token
     * @param shopId the Shopware shop ID
     * @return a new authentication token
     */
    @Nonnull String generateAppToken(@Nonnull ShopwareApp app, @Nonnull String shopId);

    /**
     * Validates whether the provided token is valid for the given app and shop combination.
     * <p>
     * Use this method in {@code @PreAuthorize} annotations to secure custom endpoints.
     *
     * @param app    the app to validate against
     * @param shopId the Shopware shop ID to validate against
     * @param token  the token to validate
     * @return true if the token is valid, false otherwise
     */
    boolean isAppTokenValid(@Nullable ShopwareApp app, @Nullable String shopId, @Nullable String token);

    /**
     * Checks if the given token has expired based on its embedded timestamp.
     *
     * @param token the token to check for expiration
     * @return true if the token has expired, false otherwise
     */
    boolean isTokenExpired(@Nonnull String token);
}
