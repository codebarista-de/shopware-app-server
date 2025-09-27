package de.codebarista.shopware.appserver.api.dto.lifecycle;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ShopwareAppLifecycleEventDto(
        @JsonProperty(required = true) String timestamp,
        @JsonProperty(required = true) ShopwareAppLifecycleDataDto data,
        @JsonProperty(required = true) ShopwareAppLifecycleEventSourceDto source
) {
}