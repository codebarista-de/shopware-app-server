package de.codebarista.shopware.appbackend.sdk.api.dto.lifecycle;

import com.fasterxml.jackson.databind.JsonNode;

public record ShopwareAppLifecycleDataDto(String event, JsonNode payload) {
}
