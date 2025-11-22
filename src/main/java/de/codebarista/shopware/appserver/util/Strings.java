package de.codebarista.shopware.appserver.util;

import jakarta.annotation.Nullable;

/**
 * Utility class for common string operations.
 * <p>
 * Provides null-safe helper methods for string validation and manipulation.
 */
public class Strings {
    private Strings() {

    }

    /**
     * Checks if a string is null or blank (empty or contains only whitespace).
     *
     * @param string the string to check
     * @return true if the string is null or blank, false otherwise
     */
    public static boolean isNullOrBlank(String string) {
        return string == null || string.isBlank();
    }

    /**
     * Checks if a string is not null and not blank.
     *
     * @param string the string to check
     * @return true if the string has content, false if null or blank
     */
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

    /**
     * Converts a string to lowercase in a null-safe manner.
     *
     * @param string the string to convert
     * @return the lowercase string, or null if the input was null
     */
    @Nullable
    public static String toLower(@Nullable String string) {
        if (string == null) {
            return null;
        }
        return string.toLowerCase();
    }

    /**
     * Converts a string to uppercase in a null-safe manner.
     *
     * @param string the string to convert
     * @return the uppercase string, or null if the input was null
     */
    @Nullable
    public static String toUpper(@Nullable String string) {
        if (string == null) {
            return null;
        }
        return string.toUpperCase();
    }
}
