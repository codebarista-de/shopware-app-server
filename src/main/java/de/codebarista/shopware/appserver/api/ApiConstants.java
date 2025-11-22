package de.codebarista.shopware.appserver.api;

/**
 * Constants for Shopware App Server API interactions.
 * <p>
 * Contains HTTP header names and parameter names used in communication between
 * Shopware shops and app backends.
 */
public final class ApiConstants {
    /**
     * HTTP header name for the signature sent by Shopware to the app backend.
     * The signature is an HMAC-SHA256 hash of the request body signed with the app secret.
     */
    public static final String SHOPWARE_SHOP_SIGNATURE_HEADER = "shopware-shop-signature";

    /**
     * HTTP header name for the signature sent by the app backend to Shopware.
     * The signature is an HMAC-SHA256 hash of the response body signed with the app secret.
     */
    public static final String SHOPWARE_APP_SIGNATURE_HEADER = "shopware-app-signature";

    /**
     * HTTP header name containing the locale of the user who triggered the request.
     * Example value: "en-GB"
     */
    public static final String SHOPWARE_USER_LANGUAGE_HEADER = "sw-user-language";

    /**
     * HTTP header name containing the Shopware language ID for the current context.
     * This is the UUID of the language entity in Shopware.
     */
    public static final String SHOPWARE_LANGUAGE_ID_HEADER = "sw-context-language";
}
