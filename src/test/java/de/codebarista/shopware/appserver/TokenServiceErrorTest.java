package de.codebarista.shopware.appserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.codebarista.shopware.appserver.exception.InvalidTokenException;
import de.codebarista.shopware.appserver.model.ShopwareShopEntity;
import de.codebarista.shopware.appserver.service.ShopManagementService;
import de.codebarista.shopware.appserver.service.SignatureService;
import de.codebarista.shopware.appserver.service.TokenServiceImpl;
import de.codebarista.shopware.testutils.TestAppA;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Error scenario tests for TokenServiceImpl focusing on edge cases and error handling.
 */
public class TokenServiceErrorTest {

    private TokenServiceImpl tokenService;
    private ShopManagementService shopManagementService;
    private TestAppA testApp;

    @BeforeEach
    public void setup() {
        shopManagementService = mock(ShopManagementService.class);
        SignatureService signatureService = new SignatureService(new ObjectMapper());
        tokenService = new TokenServiceImpl(shopManagementService, signatureService);
        testApp = new TestAppA();
    }

    @Test
    public void generateAppTokenWithNullAppThrows() {
        assertThatThrownBy(() -> tokenService.generateAppToken(null, "shopId"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Data or secret to sign cannot be null");
    }

    @Test
    public void generateAppTokenWithNullShopIdThrows() {
        assertThatThrownBy(() -> tokenService.generateAppToken(testApp, null))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Data or secret to sign cannot be null");
    }

    @Test
    public void generateAppTokenWithBothNullThrows() {
        assertThatThrownBy(() -> tokenService.generateAppToken(null, null))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Data or secret to sign cannot be null");
    }

    @Test
    public void isAppTokenValidWithNullInputsGracefully() {
        // All null inputs should return false, not throw exceptions
        assertThat(tokenService.isAppTokenValid(null, null, null)).isFalse();
        assertThat(tokenService.isAppTokenValid(testApp, null, "token")).isFalse();
        assertThat(tokenService.isAppTokenValid(testApp, "shopId", null)).isFalse();
        assertThat(tokenService.isAppTokenValid(null, "shopId", "token")).isFalse();
    }

    @Test
    public void isAppTokenValidWithInvalidTokenLength() {
        // Token must be exactly 148 chars (20 + 64 + 64)
        assertThat(tokenService.isAppTokenValid(testApp, "shopId", "")).isFalse();
        assertThat(tokenService.isAppTokenValid(testApp, "shopId", "short")).isFalse();
        assertThat(tokenService.isAppTokenValid(testApp, "shopId", "x".repeat(147))).isFalse();
        assertThat(tokenService.isAppTokenValid(testApp, "shopId", "x".repeat(149))).isFalse();
    }

    @Test
    public void isAppTokenValidWithNonExistentShop() {
        when(shopManagementService.getShopById(any(), any())).thenReturn(Optional.empty());

        String validLengthToken = "x".repeat(148);
        assertThat(tokenService.isAppTokenValid(testApp, "nonExistentShop", validLengthToken)).isFalse();
    }

    @Test
    public void isTokenExpiredWithTooShortToken() {
        // Tokens shorter than 20 characters should be considered expired
        assertThat(tokenService.isTokenExpired("")).isTrue();
        assertThat(tokenService.isTokenExpired("short")).isTrue();
        assertThat(tokenService.isTokenExpired("x".repeat(19))).isTrue();
    }

    @Test
    public void isTokenExpiredWithInvalidTimestamp() {
        // Non-numeric timestamp should be considered expired
        String invalidToken = "abcdefghijklmnopqrst" + "x".repeat(128); // 20 non-numeric + rest
        assertThat(tokenService.isTokenExpired(invalidToken)).isTrue();
    }

    @Test
    public void isTokenExpiredWithNegativeTimestamp() {
        // Negative timestamp should be considered expired
        String negativeTimestamp = String.format("%020d", -1L); // -1 padded to 20 chars
        String tokenWithNegativeTime = negativeTimestamp + "x".repeat(128);
        assertThat(tokenService.isTokenExpired(tokenWithNegativeTime)).isTrue();
    }

    @Test
    public void isTokenExpiredWithFutureTimestamp() {
        // Future timestamp should not be expired
        long futureTime = System.currentTimeMillis() + Duration.ofHours(1).toMillis();
        String futureTimestamp = String.format("%020d", futureTime);
        String tokenWithFutureTime = futureTimestamp + "x".repeat(128);
        assertThat(tokenService.isTokenExpired(tokenWithFutureTime)).isFalse();
    }

    @Test
    public void isTokenExpiredWithExactlyExpiredToken() {
        // Token that expired exactly now should be considered expired
        long expiredTime = System.currentTimeMillis() - TokenServiceImpl.TOKEN_TTL_SECONDS * 1000;
        String expiredTimestamp = String.format("%020d", expiredTime);
        String expiredToken = expiredTimestamp + "x".repeat(128);
        assertThat(tokenService.isTokenExpired(expiredToken)).isTrue();
    }

    @Test
    public void isTokenExpiredWithAlmostExpiredToken() {
        // Token that expires in 1 second should still be valid
        long almostExpiredTime = System.currentTimeMillis() - TokenServiceImpl.TOKEN_TTL_SECONDS * 1000 + 1000;
        String almostExpiredTimestamp = String.format("%020d", almostExpiredTime);
        String almostExpiredToken = almostExpiredTimestamp + "x".repeat(128);
        assertThat(tokenService.isTokenExpired(almostExpiredToken)).isFalse();
    }

    @Test
    public void isTokenExpiredWithVeryOldToken() {
        // Very old token should definitely be expired
        long veryOldTime = System.currentTimeMillis() - Duration.ofDays(365).toMillis();
        String veryOldTimestamp = String.format("%020d", veryOldTime);
        String veryOldToken = veryOldTimestamp + "x".repeat(128);
        assertThat(tokenService.isTokenExpired(veryOldToken)).isTrue();
    }

    @Test
    public void isTokenExpiredWithZeroTimestamp() {
        // Token with timestamp 0 should be expired
        String zeroTimestamp = "00000000000000000000"; // 20 zeros
        String tokenWithZeroTime = zeroTimestamp + "x".repeat(128);
        assertThat(tokenService.isTokenExpired(tokenWithZeroTime)).isTrue();
    }

    @Test
    public void isTokenExpiredWithMixedValidInvalidCharacters() {
        // Mix of numbers and letters in timestamp should be invalid
        String mixedTimestamp = "1234567890abcdefghij"; // 10 numbers + 10 letters
        String tokenWithMixedTime = mixedTimestamp + "x".repeat(128);
        assertThat(tokenService.isTokenExpired(tokenWithMixedTime)).isTrue();
    }

    @Test
    public void generateAndValidateTokenFlowWithValidShop() {
        // Setup valid shop
        String shopId = "testShop";
        ShopwareShopEntity shop = new ShopwareShopEntity(testApp.getAppKey(), shopId);
        shop.setPendingRegistration("testSecret", "https://my-shop.de");
        shop.confirmPendingRegistrationAndAddShopApiSecrets("apiKey", "apiSecret");

        when(shopManagementService.getShopByIdOrThrow(testApp, shopId)).thenReturn(shop);
        when(shopManagementService.getShopById(testApp, shopId)).thenReturn(Optional.of(shop));

        // Generate token
        String token = tokenService.generateAppToken(testApp, shopId);
        assertThat(token).isNotNull().hasSize(148);

        // Token should not be expired immediately
        assertThat(tokenService.isTokenExpired(token)).isFalse();

        // Token should be valid
        assertThat(tokenService.isAppTokenValid(testApp, shopId, token)).isTrue();
    }
}