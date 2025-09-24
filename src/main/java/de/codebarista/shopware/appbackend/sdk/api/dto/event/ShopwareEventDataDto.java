package de.codebarista.shopware.appbackend.sdk.api.dto.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ShopwareEventDataDto(List<ShopwareEventPayloadDto> payload, @JsonProperty(required = true) String event) {

}
