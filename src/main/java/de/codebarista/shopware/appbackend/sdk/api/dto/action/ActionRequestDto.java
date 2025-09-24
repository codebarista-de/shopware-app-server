package de.codebarista.shopware.appbackend.sdk.api.dto.action;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ActionRequestDto(@JsonProperty(required = true) ActionRequestSourceDto source,
                               @JsonProperty(required = true) ActionRequestDataDto data,
                               @JsonProperty(required = true) ActionRequestMetaDto meta) {
}
