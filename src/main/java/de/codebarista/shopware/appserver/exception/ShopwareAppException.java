package de.codebarista.shopware.appserver.exception;

/**
 * Base exception for all Shopware App Server related errors.
 */
public class ShopwareAppException extends RuntimeException {
    public ShopwareAppException(String message) {
        super(message);
    }

    public ShopwareAppException(String message, Throwable cause) {
        super(message, cause);
    }
}