package de.codebarista.shopware.appserver.service.dto.search;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class Filter {
    @JsonProperty("type")
    private final String type;

    protected Filter(String type) {
        this.type = type;
    }
}
