package de.codebarista.shopware.appserver.exception;

import de.codebarista.shopware.appserver.service.ShopwareApp;

public class NoSuchShopException extends ShopwareAppException {
    public NoSuchShopException(String message) {
        super(message);
    }

    public NoSuchShopException(String message, Throwable cause) {
        super(message, cause);
    }

    public static NoSuchShopException byId(ShopwareApp app, String id) {
        return byId(app.getAppKey(), id);
    }

    public static NoSuchShopException byId(String appKey, String id) {
        return new NoSuchShopException("Could not find any shop for " + appKey + " with ID: " + id);
    }

    public static NoSuchShopException byInternalId(Long id) {
        return new NoSuchShopException("Could not find shop with internal ID: " + id);
    }

    public static NoSuchShopException byUrl(ShopwareApp app, String url) {
        return new NoSuchShopException("Could not find any shop for " + app + " with URL: " + url);
    }
}
