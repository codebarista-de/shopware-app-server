package de.codebarista.shopware.appserver.api.dto.action;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ActionRequestDto(@JsonProperty(required = true) ActionRequestSourceDto source,
                               @JsonProperty(required = true) ActionRequestDataDto data,
                               @JsonProperty(required = true) ActionRequestMetaDto meta) {
}
