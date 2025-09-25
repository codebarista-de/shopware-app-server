package de.codebarista.shopware.appbackend.sdk.exception;

/**
 * Exception thrown when signature verification or calculation fails.
 */
public class InvalidSignatureException extends ShopwareAppException {
    public InvalidSignatureException(String message) {
        super(message);
    }

    public InvalidSignatureException(String message, Throwable cause) {
        super(message, cause);
    }
}