package de.codebarista.shopware.appbackend.sdk.util;

import jakarta.annotation.Nullable;

import java.util.Locale;

public class LocaleUtils {
    private LocaleUtils() {
    }

    /**
     * Converts a Shopware language string to a Java Locale.
     * Shopware language format: "en-GB", "de-DE", etc.
     */
    public static Locale getLocale(@Nullable String shopwareLanguage) {
        if (shopwareLanguage == null) {
            return Locale.getDefault();
        }

        String[] parts = shopwareLanguage.split("-");
        if (parts.length == 2) {
            return new Locale(parts[0], parts[1]);
        } else if (parts.length == 1) {
            return new Locale(parts[0]);
        }

        return Locale.getDefault();
    }
}