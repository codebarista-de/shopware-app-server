package de.codebarista.shopware.appbackend.sdk.service.dto.search;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class Filter {
    @JsonProperty("type")
    private final String type;

    protected Filter(String type) {
        this.type = type;
    }
}
