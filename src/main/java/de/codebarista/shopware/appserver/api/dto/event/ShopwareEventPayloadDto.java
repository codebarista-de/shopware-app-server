package de.codebarista.shopware.appserver.api.dto.event;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ShopwareEventPayloadDto(@JsonProperty(required = true) String entity, Operation operation, String primaryKey) {

    public enum Operation {
        @JsonProperty("insert") INSERT,
        @JsonProperty("update") UPDATE,
        @JsonProperty("delete") DELETE,
        @JsonEnumDefaultValue UNKNOWN
    }
}
