package de.codebarista.shopware.appserver.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Root DTO for Shopware webhook events.
 * <p>
 * This record encapsulates all information about an event triggered in Shopware
 * and sent to the app backend via webhooks configured in the manifest.xml.
 *
 * @param timestamp the ISO-8601 formatted timestamp when the event was triggered
 * @param data      the event data containing the event name and payload
 * @param source    information about the shop and context from which the event originated
 */
public record ShopwareEventDto(
        @JsonProperty(required = true) String timestamp,
        @JsonProperty(required = true) ShopwareEventDataDto data,
        @JsonProperty(required = true) ShopwareEventSourceDto source
) {
}
