package de.codebarista.shopware.appserver.service.dto.search;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines sorting criteria for Shopware search queries.
 * <p>
 * Use this class to specify how search results should be ordered.
 * Supports ascending and descending order with optional natural sorting.
 * <p>
 * Example usage:
 * <pre>
 * Sorting.byField("name").ascending()
 * Sorting.byField("createdAt").descending()
 * </pre>
 */
public class Sorting {
    @JsonProperty("field")
    private String field;

    @JsonProperty("order")
    private Order order = Order.ASCENDING;

    @JsonProperty("naturalSorting")
    private boolean naturalSorting = true;

    private Sorting(String field) {
        this.field = field;
    }

    /**
     * Creates a new sorting criterion for the specified field.
     *
     * @param field the name of the field to sort by
     * @return a new Sorting instance
     */
    public static Sorting byField(String field) {
        return new Sorting(field);
    }

    /**
     * Sets the sort order to descending.
     *
     * @return this Sorting instance for method chaining
     */
    public Sorting descending() {
        order = Order.DESCENDING;
        return this;
    }

    /**
     * Sets the sort order to ascending.
     *
     * @return this Sorting instance for method chaining
     */
    public Sorting ascending() {
        order = Order.ASCENDING;
        return this;
    }
}
