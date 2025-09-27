package de.codebarista.shopware.appserver.api.dto.registration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShopwareAppRegistrationResponseDto {
    @JsonProperty("proof")
    private final String proof;
    @JsonProperty("secret")
    private final String shopSecret;
    @JsonProperty("confirmation_url")
    private final String confirmationUrl;
    @JsonProperty("error")
    private final String error;

    @JsonCreator
    private ShopwareAppRegistrationResponseDto(
            @JsonProperty("proof") String proof,
            @JsonProperty("secret") String shopSecret,
            @JsonProperty("confirmation_url") String confirmationUrl,
            @JsonProperty("error") String error) {
        this.proof = proof;
        this.shopSecret = shopSecret;
        this.confirmationUrl = confirmationUrl;
        this.error = error;
    }

    public static ShopwareAppRegistrationResponseDto error(String errorMessage) {
        return new ShopwareAppRegistrationResponseDto(null, null, null, errorMessage);
    }

    public static ShopwareAppRegistrationResponseDto success(String proof, String shopSecret, String confirmationUr) {
        return new ShopwareAppRegistrationResponseDto(proof, shopSecret, confirmationUr, null);
    }

    @Override
    public String toString() {
        return "ShopwareAppRegistrationResponseDto{" +
                "proof='" + proof + '\'' +
                ", confirmationUrl='" + confirmationUrl + '\'' +
                ", error='" + error + '\'' +
                '}';
    }

    /**
     * Gets the {@link #proof}.
     */
    public String getProof() {
        return proof;
    }

    /**
     * Gets the {@link #shopSecret}.
     */
    public String getShopSecret() {
        return shopSecret;
    }

    /**
     * Gets the {@link #confirmationUrl}.
     */
    public String getConfirmationUrl() {
        return confirmationUrl;
    }

    /**
     * Gets the {@link #error}.
     */
    public String getError() {
        return error;
    }
}

