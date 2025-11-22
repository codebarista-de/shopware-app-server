package de.codebarista.shopware.appserver.service.dto.search;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Filter that matches entities where a field equals a specific value.
 * <p>
 * This filter performs an exact match comparison.
 * The following SQL statement is executed in the background:
 * {@code WHERE fieldName = 'value'}
 *
 * @param <T> the type of the value to match (e.g., {@link String}, {@link Integer}, {@link Boolean})
 */
public class EqualsFilter<T> extends Filter {
    @JsonProperty("field")
    private final String field;

    @JsonProperty("value")
    private final T value;

    /**
     * Constructs an equals filter.
     *
     * @param field the name of the field to filter on
     * @param value the value that the field must equal
     */
    public EqualsFilter(String field, T value) {
        super("equalsAny");
        this.field = field;
        this.value = value;
    }
}
