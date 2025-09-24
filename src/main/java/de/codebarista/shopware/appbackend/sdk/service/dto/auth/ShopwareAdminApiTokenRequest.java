package de.codebarista.shopware.appbackend.sdk.service.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ShopwareAdminApiTokenRequest {
    @JsonProperty("grant_type")
    private final String grantType;
    @JsonProperty("client_id")
    private final String clientId;
    @JsonProperty("client_secret")
    private final String clientSecret;

    public ShopwareAdminApiTokenRequest(String clientId, String clientSecret) {
        this.grantType = "client_credentials";
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }
}
