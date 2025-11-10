package de.codebarista.shopware.appserver;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public interface TokenService {
    @Nonnull String generateAppToken(@Nonnull ShopwareApp app, @Nonnull String shopId);
    boolean isAppTokenValid(@Nullable ShopwareApp app, @Nullable String shopId, @Nullable String token);

    /**
     * Checks if the given token has expired based on its embedded timestamp.
     *
     * @param token the token to check for expiration
     * @return true if the token has expired, false otherwise
     */
    boolean isTokenExpired(@Nonnull String token);
}
