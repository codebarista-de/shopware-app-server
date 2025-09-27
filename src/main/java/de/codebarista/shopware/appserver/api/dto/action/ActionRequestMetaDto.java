package de.codebarista.shopware.appserver.api.dto.action;

public record ActionRequestMetaDto(
        Long timestamp,
        String reference,
        String language
) {
}
