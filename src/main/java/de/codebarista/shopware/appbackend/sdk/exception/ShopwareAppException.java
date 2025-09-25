package de.codebarista.shopware.appbackend.sdk.exception;

/**
 * Base exception for all Shopware App Backend SDK related errors.
 */
public class ShopwareAppException extends RuntimeException {
    public ShopwareAppException(String message) {
        super(message);
    }

    public ShopwareAppException(String message, Throwable cause) {
        super(message, cause);
    }
}