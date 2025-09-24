package de.codebarista.shopware.appbackend.sdk.util;

import jakarta.annotation.Nullable;

public class StringUtils {
    private StringUtils() {

    }

    public static boolean isNullOrBlank(String string) {
        return string == null || string.isBlank();
    }

    public static boolean isNotNullOrBlank(String string) {
        return !isNullOrBlank(string);
    }

    /**
     * Returns the default value, if the actualValue is null or blank. May return null, if the default value is null.
     *
     * @param actualValue  the actual value
     * @param defaultValue the default value
     */
    @Nullable
    public static String valueOrDefault(@Nullable String actualValue, @Nullable String defaultValue) {
        return (actualValue == null || actualValue.isBlank()) ? defaultValue : actualValue;
    }

    @Nullable
    public static String toLower(@Nullable String string) {
        if (string == null) {
            return null;
        }
        return string.toLowerCase();
    }

    @Nullable
    public static String toUpper(@Nullable String string) {
        if (string == null) {
            return null;
        }
        return string.toUpperCase();
    }
}
