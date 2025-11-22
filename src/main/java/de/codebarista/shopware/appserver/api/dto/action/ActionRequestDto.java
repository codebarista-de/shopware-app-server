package de.codebarista.shopware.appserver.api.dto.action;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Root DTO for Shopware action button requests.
 * <p>
 * This record encapsulates all information about an action triggered from the Shopware Administration
 * when a user clicks a custom action button defined in the manifest.xml.
 *
 * @param source information about the shop from which the action was triggered
 * @param data   the action data including entity, action name, and selected IDs
 * @param meta   metadata about the request including timestamp and language information
 */
public record ActionRequestDto(@JsonProperty(required = true) ActionRequestSourceDto source,
                               @JsonProperty(required = true) ActionRequestDataDto data,
                               @JsonProperty(required = true) ActionRequestMetaDto meta) {
}
