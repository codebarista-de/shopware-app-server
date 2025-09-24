package de.codebarista.shopware.appbackend.sdk.api.dto.action;

public record ActionRequestMetaDto(
        Long timestamp,
        String reference,
        String language
) {
}
