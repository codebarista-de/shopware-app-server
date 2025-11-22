package de.codebarista.shopware.appserver.exception;

import de.codebarista.shopware.appserver.ShopwareApp;

/**
 * Exception thrown when a requested shop is not found in the app server's database.
 * <p>
 * This exception is typically thrown when attempting to access shop data or
 * perform operations on a shop that has not been registered or has been deleted.
 */
public class NoSuchShopException extends ShopwareAppException {
    public NoSuchShopException(String message) {
        super(message);
    }

    public NoSuchShopException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates an exception for a shop not found by Shopware shop ID.
     *
     * @param app the app for which the shop was not found
     * @param id  the Shopware shop ID that was not found
     * @return a new NoSuchShopException
     */
    public static NoSuchShopException byId(ShopwareApp app, String id) {
        return byId(app.getAppKey(), id);
    }

    /**
     * Creates an exception for a shop not found by Shopware shop ID.
     *
     * @param appKey the app key for which the shop was not found
     * @param id     the Shopware shop ID that was not found
     * @return a new NoSuchShopException
     */
    public static NoSuchShopException byId(String appKey, String id) {
        return new NoSuchShopException("Could not find any shop for " + appKey + " with ID: " + id);
    }

    /**
     * Creates an exception for a shop not found by internal database ID.
     *
     * @param id the internal shop ID that was not found
     * @return a new NoSuchShopException
     */
    public static NoSuchShopException byInternalId(Long id) {
        return new NoSuchShopException("Could not find shop with internal ID: " + id);
    }

    /**
     * Creates an exception for a shop not found by shop URL.
     *
     * @param app the app for which the shop was not found
     * @param url the shop URL that was not found
     * @return a new NoSuchShopException
     */
    public static NoSuchShopException byUrl(ShopwareApp app, String url) {
        return new NoSuchShopException("Could not find any shop for " + app + " with URL: " + url);
    }
}
