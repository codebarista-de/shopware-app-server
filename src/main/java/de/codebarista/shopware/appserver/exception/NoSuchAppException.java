package de.codebarista.shopware.appserver.exception;

/**
 * Exception thrown when an app with the specified key is not found.
 */
public class NoSuchAppException extends ShopwareAppException {
    public NoSuchAppException(String message) {
        super(message);
    }

    public static NoSuchAppException byKey(String appKey) {
        return new NoSuchAppException(String.format("No such app %s", appKey));
    }
}