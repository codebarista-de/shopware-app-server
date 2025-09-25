package de.codebarista.shopware.appbackend.sdk.service;

import de.codebarista.shopware.appbackend.sdk.exception.InvalidTokenException;
import de.codebarista.shopware.appbackend.sdk.model.ShopwareShopEntity;
import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component("tokenService")
public class TokenServiceImpl implements TokenService {
    private static final int TOKEN_TIME_LENGTH = 20;
    private static final int TOKEN_HASH_LENGTH = 64;
    private static final int TOKEN_SIGNATURE_LENGTH = 64;
    public static final long TOKEN_TTL_SECONDS = Duration.ofHours(1).toSeconds();
    private static final long TOKEN_TTL_MILLIS = TOKEN_TTL_SECONDS * 1000;

    private final ShopManagementService shopManagementService;
    private final SignatureService signatureService;

    public TokenServiceImpl(ShopManagementService shopManagementService,
                            SignatureService signatureService) {
        this.shopManagementService = shopManagementService;
        this.signatureService = signatureService;
    }

    @Override
    public String generateAppToken(@Nonnull ShopwareApp app, @Nonnull String shopId) {
        //noinspection ConstantValue
        if (shopId == null || app == null) {
            throw new InvalidTokenException("Data or secret to sign cannot be null");
        }

        String secret = shopManagementService.getShopByIdOrThrow(app, shopId).getShopSecret();

        long currentTimeMillis = System.currentTimeMillis();
        String paddedTime = String.format("%1$" + TOKEN_TIME_LENGTH + "s", currentTimeMillis).replace(' ', '0');
        String hash = signatureService.hash(shopId + app.getAppKey());
        String dataToSign = paddedTime + hash;
        String signature = signatureService.calculateSignature(dataToSign, secret);
        return paddedTime + hash + signature;
    }

    @Override
    public boolean isAppTokenValid(ShopwareApp app, String shopId, String token) {
        if (shopId == null
                || app == null
                || token == null
                || token.length() != TOKEN_TIME_LENGTH + TOKEN_HASH_LENGTH + TOKEN_SIGNATURE_LENGTH) {
            return false;
        }

        return shopManagementService.getShopById(app, shopId)
                .map(shop -> isAppTokenValid(app, shop, token))
                .orElse(false);
    }

    private boolean isAppTokenValid(ShopwareApp app, ShopwareShopEntity shop, String token) {
        String signature = token.substring(TOKEN_TIME_LENGTH + TOKEN_HASH_LENGTH);
        String timestampAndHash = token.substring(0, TOKEN_TIME_LENGTH + TOKEN_HASH_LENGTH);
        String calculatedSignature = signatureService.calculateSignature(timestampAndHash, shop.getShopSecret());
        if (!calculatedSignature.equals(signature)) {
            return false;
        }

        String tokenHash = token.substring(TOKEN_TIME_LENGTH, TOKEN_TIME_LENGTH + TOKEN_HASH_LENGTH);
        String calculatedHash = signatureService.hash(shop.getShopId() + app.getAppKey());
        if (!calculatedHash.equals(tokenHash)) {
            return false;
        }

        return !isTokenExpired(token);
    }

    /**
     * Checks if the given token has expired based on its embedded timestamp.
     *
     * @param token the token to check for expiration
     * @return true if the token has expired, false otherwise
     */
    boolean isTokenExpired(@Nonnull String token) {
        if (token.length() < TOKEN_TIME_LENGTH) {
            return true; // Invalid token format, consider expired
        }

        String tokenTimeString = token.substring(0, TOKEN_TIME_LENGTH);
        try {
            long tokenTimeMillis = Long.parseLong(tokenTimeString);
            long currentTimeMillis = System.currentTimeMillis();
            return tokenTimeMillis + TOKEN_TTL_MILLIS <= currentTimeMillis;
        } catch (NumberFormatException e) {
            return true; // Invalid timestamp format, consider expired
        }
    }
}
