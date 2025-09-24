package de.codebarista.shopware.appbackend.sdk.service.dto.search;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Order {
    @JsonProperty("ASC")
    ASCENDING,
    @JsonProperty("DESC")
    DESCENDING
}
