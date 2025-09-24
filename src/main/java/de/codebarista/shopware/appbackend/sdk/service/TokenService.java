package de.codebarista.shopware.appbackend.sdk.service;

import jakarta.annotation.Nonnull;

public interface TokenService {
    String generateAppToken(@Nonnull ShopwareApp app, @Nonnull String shopId);
    boolean isAppTokenValid(ShopwareApp app, String shopId, String token);
}
