package de.codebarista.shopware.appbackend.sdk.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ShopwareEventDto(
        @JsonProperty(required = true) String timestamp,
        @JsonProperty(required = true) ShopwareEventDataDto data,
        @JsonProperty(required = true) ShopwareEventSourceDto source
) {
}
