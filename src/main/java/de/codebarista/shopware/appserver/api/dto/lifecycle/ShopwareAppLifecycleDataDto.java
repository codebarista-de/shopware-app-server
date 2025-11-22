package de.codebarista.shopware.appserver.api.dto.lifecycle;

import com.fasterxml.jackson.databind.JsonNode;

public record ShopwareAppLifecycleDataDto(String event, JsonNode payload) {
}
