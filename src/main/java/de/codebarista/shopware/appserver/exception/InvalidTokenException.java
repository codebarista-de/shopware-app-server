package de.codebarista.shopware.appserver.exception;

/**
 * Exception thrown when token validation or generation fails.
 */
public class InvalidTokenException extends ShopwareAppException {
    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}