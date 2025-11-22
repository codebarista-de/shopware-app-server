package de.codebarista.shopware.appserver.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ShopwareEventSourceDto(
        @JsonProperty(value = "url", required = true)
        String shopUrl,
        @JsonProperty(required = true) String appVersion,
        @JsonProperty(required = true) String shopId,
        @JsonProperty(required = true) String eventId
) {
}
