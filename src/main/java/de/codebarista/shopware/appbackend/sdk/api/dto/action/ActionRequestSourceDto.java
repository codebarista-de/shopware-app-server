package de.codebarista.shopware.appbackend.sdk.api.dto.action;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ActionRequestSourceDto(
        @JsonProperty(value = "url", required = true)
        String shopUrl,
        @JsonProperty(required = true) String appVersion,
        @JsonProperty(required = true) String shopId
) {
}
