package de.codebarista.shopware.appbackend.sdk.exception;

public class ShopwareAccessException extends ShopwareAppException {
    public ShopwareAccessException(String message) {
        super(message);
    }

    public ShopwareAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public static ShopwareAccessException create(String shopId, String shopUrl) {
        return new ShopwareAccessException("Could not get access token for shop " + shopId + " (" + shopUrl + ")");
    }

    public static ShopwareAccessException create(String shopId) {
        return new ShopwareAccessException("Could not get access token for shop " + shopId);
    }
}
