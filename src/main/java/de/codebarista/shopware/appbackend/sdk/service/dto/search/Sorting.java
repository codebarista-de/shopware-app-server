package de.codebarista.shopware.appbackend.sdk.service.dto.search;

import com.fasterxml.jackson.annotation.JsonProperty;

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

    public static Sorting byField(String field) {
        return new Sorting(field);
    }

    public Sorting descending() {
        order = Order.DESCENDING;
        return this;
    }

    public Sorting ascending() {
        order = Order.ASCENDING;
        return this;
    }
}
