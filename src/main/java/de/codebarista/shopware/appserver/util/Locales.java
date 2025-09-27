package de.codebarista.shopware.appserver.util;

import jakarta.annotation.Nullable;

public class Locales {
    private Locales() {
    }

    /**
     * Converts a Shopware language string to a Java Locale.
     * Shopware language format: "en-GB", "de-DE", etc.
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