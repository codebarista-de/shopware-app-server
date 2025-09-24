package de.codebarista.shopware.appbackend.sdk.service.dto.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * A filter that restricts values of a field to a range defined by
 * exclusive lower and/or upper bounds (i.e., {@code >} and {@code <}).
 * <p>
 * At least one of the bounds must be provided. If both are specified,
 * the field value must lie within the open interval
 * {@code (greaterThan, lessThan)}.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // Matches field "price" strictly greater than 10 and strictly less than 100
 * new ExclusiveRangeFilter<>("price", 10, 100);
 *
 * // Matches field "createdAt" strictly after 2025-01-01
 * new ExclusiveRangeFilter<>("createdAt", LocalDate.parse("2025-01-01"), null);
 * }</pre>
 *
 * @param <T> the value type of the range bounds, e.g. {@link Integer}, {@link Long}, {@link Double}, {@link java.time.LocalDate}
 */
public class ExclusiveRangeFilter<T> extends Filter {
    @JsonProperty("field")
    private final String field;

    @JsonProperty("parameters")
    private final Map<String, T> range;

    /**
     * Creates a new exclusive range filter for the given field.
     *
     * @param field       the field name to apply the filter on (must not be {@code null})
     * @param greaterThan the exclusive lower bound ({@code >}), or {@code null} if unbounded below
     * @param lessThan    the exclusive upper bound ({@code <}), or {@code null} if unbounded above
     * @throws IllegalArgumentException if both {@code greaterThan} and {@code lessThan} are {@code null}
     */
    public ExclusiveRangeFilter(String field, @Nullable T greaterThan, @Nullable T lessThan) {
        super("range");

        if (greaterThan == null && lessThan == null) {
            throw new IllegalArgumentException("At least one bound must not be null");
        }

        this.field = field;
        range = new HashMap<>();
        if (greaterThan != null) {
            range.put("gt", greaterThan);
        }
        if (lessThan != null) {
            range.put("lt", lessThan);
        }
    }
}
