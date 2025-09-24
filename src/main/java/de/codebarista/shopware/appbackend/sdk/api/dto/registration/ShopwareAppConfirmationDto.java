package de.codebarista.shopware.appbackend.sdk.api.dto.registration;

public class ShopwareAppConfirmationDto {
    /**
     * The API key used to authenticate against the Shopware Admin API.
     */
    private String apiKey;

    /**
     * The secret key used to authenticate against the Shopware Admin API.
     */
    private String secretKey;

    /**
     * The Unix timestamp when the request was created.
     */
    private String timestamp;

    /**
     * The URL of the shop.
     */
    private String shopUrl;

    /**
     * The unique identifier of the shop.
     */
    private String shopId;

    ShopwareAppConfirmationDto() {

    }

    public ShopwareAppConfirmationDto(String apiKey, String secretKey, String timestamp, String shopUrl, String shopId) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.timestamp = timestamp;
        this.shopUrl = shopUrl;
        this.shopId = shopId;
    }

    /**
     * Gets the {@link #apiKey}.
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Gets the {@link #secretKey}.
     */
    public String getSecretKey() {
        return secretKey;
    }

    /**
     * Gets the {@link #timestamp}.
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the {@link #shopUrl}.
     */
    public String getShopUrl() {
        return shopUrl;
    }

    /**
     * Gets the {@link #shopId}.
     */
    public String getShopId() {
        return shopId;
    }

    @Override
    public String toString() {
        return "ShopwareAppConfirmationDto{" +
                "apiKey='" + apiKey + '\'' +
                ", secretKey='" + secretKey + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", shopUrl='" + shopUrl + '\'' +
                ", shopId='" + shopId + '\'' +
                '}';
    }
}
