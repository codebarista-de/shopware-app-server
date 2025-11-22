package de.codebarista.shopware.appserver.service.dto.search;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Abstract base class for all Shopware search filters.
 * <p>
 * Filters are used to narrow down search results when querying Shopware entities via the Admin API.
 * Concrete implementations include {@link EqualsFilter}, {@link EqualsAnyFilter},
 * {@link InclusiveRangeFilter}, and {@link ExclusiveRangeFilter}.
 */
public abstract class Filter {
    @JsonProperty("type")
    private final String type;

    /**
     * Constructs a filter with the specified type.
     *
     * @param type the Shopware filter type identifier (e.g., "equals", "equalsAny", "range")
     */
    protected Filter(String type) {
        this.type = type;
    }
}
