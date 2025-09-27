package de.codebarista.shopware.appserver.service.dto.search;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Order {
    @JsonProperty("ASC")
    ASCENDING,
    @JsonProperty("DESC")
    DESCENDING
}
