package de.codebarista.shopware.appbackend.sdk.api.dto.action;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ActionRequestDataDto(
        List<String> ids,
        String entity,
        @JsonProperty(required = true)
        String action
) {
}
