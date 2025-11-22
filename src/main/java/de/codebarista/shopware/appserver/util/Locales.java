package de.codebarista.shopware.appserver.util;

import jakarta.annotation.Nullable;

/**
 * Utility class for working with locales in the context of Shopware.
 * <p>
 * Provides methods to convert between Shopware's language format and Java's Locale.
 */
public class Locales {
    private Locales() {
    }

    /**
     * Converts a Shopware language string to a Java Locale.
     * <p>
     * Shopware uses language codes in the format "en-GB", "de-DE", etc.
     * This method parses these strings into Java Locale objects.
     * If the input is null or cannot be parsed, returns the system default locale.
     *
     * @param shopwareLanguage the Shopware language code (e.g., "en-GB", "de-DE")
     * @return a Java Locale corresponding to the language code, or the default locale
     */
    public static java.util.Locale getLocale(@Nullable String shopwareLanguage) {
        if (shopwareLanguage == null) {
            return java.util.Locale.getDefault();
        }

        String[] parts = shopwareLanguage.split("-");
        if (parts.length == 2) {
            return new java.util.Locale(parts[0], parts[1]);
        } else if (parts.length == 1) {
            return new java.util.Locale(parts[0]);
        }

        return java.util.Locale.getDefault();
    }
}