package de.codebarista.shopware.appserver.exception;

public class InvalidShopUrlException extends ShopwareAppException {
    public InvalidShopUrlException(String message) {
        super(message);
    }

    public InvalidShopUrlException(String message, Throwable cause) {
        super(message, cause);
    }
}
