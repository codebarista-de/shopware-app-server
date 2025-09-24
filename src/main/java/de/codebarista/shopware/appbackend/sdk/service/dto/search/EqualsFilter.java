package de.codebarista.shopware.appbackend.sdk.service.dto.search;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EqualsFilter<T> extends Filter {
    @JsonProperty("field")
    private final String field;

    @JsonProperty("value")
    private final T value;

    public EqualsFilter(String field, T value) {
        super("equalsAny");
        this.field = field;
        this.value = value;
    }
}
