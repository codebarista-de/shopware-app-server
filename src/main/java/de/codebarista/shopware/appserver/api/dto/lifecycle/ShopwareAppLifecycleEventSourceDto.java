package de.codebarista.shopware.appserver.api.dto.lifecycle;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ShopwareAppLifecycleEventSourceDto(
        @JsonProperty(value = "url", required = true)
        String shopUrl,
        @JsonProperty(required = true) String appVersion,
        @JsonProperty(required = true) String shopId,
        @JsonProperty(required = true) String eventId
) {
}
