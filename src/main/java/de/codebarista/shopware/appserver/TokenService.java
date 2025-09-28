package de.codebarista.shopware.appserver;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public interface TokenService {
    @Nonnull String generateAppToken(@Nonnull ShopwareApp app, @Nonnull String shopId);
    boolean isAppTokenValid(@Nullable ShopwareApp app, @Nullable String shopId, @Nullable String token);
    boolean isTokenExpired(@Nonnull String token);
}
