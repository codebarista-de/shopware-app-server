package de.codebarista.shopware.appserver.service.dto.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * A filter that restricts values of a field to a range defined by
 * inclusive lower and/or upper bounds (i.e., {@code >=} and {@code <=}).
 * <p>
 * At least one of the bounds must be provided. If both are specified,
 * the field value must lie within the closed interval
 * {@code [greaterThanEqual, lessThanEqual]}.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // Matches field "price" between 10 and 100 (inclusive)
 * new InclusiveRangeFilter<>("price", 10, 100);
 *
 * // Matches field "createdAt" after or equal to 2025-01-01
 * new InclusiveRangeFilter<>("createdAt", LocalDate.parse("2025-01-01"), null);
 * }</pre>
 *
 * @param <T> the value type of the range bounds, e.g. {@link Integer}, {@link Long}, {@link Double}, {@link java.time.LocalDate}
 */
public class InclusiveRangeFilter<T> extends Filter {
    @JsonProperty("field")
    private final String field;

    @JsonProperty("parameters")
    private final Map<String, T> range;

    /**
     * Creates a new inclusive range filter for the given field.
     *
     * @param field            the field name to apply the filter on (must not be {@code null})
     * @param greaterThanEqual the inclusive lower bound ({@code >=}), or {@code null} if unbounded below
     * @param lessThanEqual    the inclusive upper bound ({@code <=}), or {@code null} if unbounded above)
     * @throws IllegalArgumentException if both {@code greaterThanEqual} and {@code lessThanEqual} are {@code null}
     */
    public InclusiveRangeFilter(String field, @Nullable T greaterThanEqual, @Nullable T lessThanEqual) {
        super("range");

        if (greaterThanEqual == null && lessThanEqual == null) {
            throw new IllegalArgumentException("At least one bound must not be null");
        }

        this.field = field;
        range = new HashMap<>();
        if (greaterThanEqual != null) {
            range.put("gte", greaterThanEqual);
        }
        if (lessThanEqual != null) {
            range.put("lte", lessThanEqual);
        }
    }
}
