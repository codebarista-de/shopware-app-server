package de.codebarista.shopware.appbackend.sdk.service.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ShopwareAdminApiAccessTokenDto {
    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private Long expiresIn;

    @JsonProperty("access_token")
    private String accessToken;

    public ShopwareAdminApiAccessTokenDto() {
    }

    public ShopwareAdminApiAccessTokenDto(String tokenType, Long expiresIn, String accessToken) {
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.accessToken = accessToken;
    }

    /**
     * Gets the {@link #tokenType}.
     */
    public String getTokenType() {
        return tokenType;
    }

    /**
     * Gets the {@link #expiresIn}.
     */
    public Long getExpiresIn() {
        return expiresIn;
    }

    /**
     * Gets the {@link #accessToken}.
     */
    public String getAccessToken() {
        return accessToken;
    }
}
