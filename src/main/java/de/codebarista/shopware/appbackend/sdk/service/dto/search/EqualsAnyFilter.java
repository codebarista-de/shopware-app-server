package de.codebarista.shopware.appbackend.sdk.service.dto.search;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The EqualsAny filter allows you to filter a field where at least one of the defined values matches exactly.
 * The following SQL statement is executed in the background:
 * WHERE productNumber IN ('3fed029475fa4d4585f3a119886e0eb1', '77d26d011d914c3aa2c197c81241a45b').
 *
 * @param <T> the type of the filtered value, e.g. {@link String}, {@link Integer}, {@link Long}, {@link Double}.
 */
public class EqualsAnyFilter<T> extends Filter {
    @JsonProperty("field")
    private final String field;

    @JsonProperty("value")
    private final List<T> values = new ArrayList<>();

    public EqualsAnyFilter(String field) {
        super("equalsAny");
        this.field = field;
    }

    public EqualsAnyFilter(String field, Collection<T> values) {
        this(field);
        this.values.addAll(values);
    }

    public void addValue(T value) {
        values.add(value);
    }

}
