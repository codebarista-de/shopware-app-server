package de.codebarista.shopware.appbackend.sdk.exception;

/**
 * Exception thrown when signature service initialization fails.
 */
public class SignatureInitializationException extends ShopwareAppException {
    public SignatureInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}