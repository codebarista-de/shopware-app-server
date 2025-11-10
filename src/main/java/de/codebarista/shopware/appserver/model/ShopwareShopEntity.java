package de.codebarista.shopware.appserver.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "SHOPWARE_SHOP")
public class ShopwareShopEntity {
    @Column(name = "APP_KEY", nullable = false)
    String appKey;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private long id;

    @Column(name = "SHOP_ID", nullable = false, unique = true)
    private String shopId;

    /**
     * Host of shop which identifies the shop, e.g. "myshop.mydomain.de"
     */
    @Column(name = "SHOP_HOST", nullable = false)
    private String shopHost;

    /**
     * URL of shop to make requests to , e.g. "https://myshop.mydomain.de"
     */
    @Column(name = "SHOP_REQUEST_URL", nullable = false)
    private String shopRequestUrl;

    @Column(name = "SHOP_SECRET", nullable = false)
    private String shopSecret;

    @Column(name = "REGISTRATION_REQUESTED_AT", nullable = false)
    private OffsetDateTime registrationRequestedAt;

    @Column(name = "CONFIRMED", nullable = false)
    private boolean registrationConfirmed;

    @Column(name = "SHOP_ADMIN_API_KEY")
    private String shopAdminApiKey; // = client_id

    @Column(name = "SHOP_ADMIN_SECRET_KEY")
    private String shopAdminApiSecretKey; // = client_secret

    @Column(name = "REGISTRATION_CONFIRMED_AT")
    private OffsetDateTime registrationConfirmedAt;

    @Column(name = "APP_VERSION")
    private String appVersion;

    @Column(name = "APP_VERSION_LAST_UPDATED_AT")
    private OffsetDateTime appVersionLastUpdatedAt;

    @Column(name = "SHOPWARE_VERSION")
    private String shopwareVersion;

    @Column(name = "SHOPWARE_VERSION_LAST_UPDATED_AT")
    private OffsetDateTime shopwareVersionLastUpdatedAt;

    @Column(name = "LAST_USED_AT")
    private OffsetDateTime lastUsedAt;

    // Used from Thymeleaf template
    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    ShopwareShopEntity() {
    }

    public ShopwareShopEntity(String appKey, String shopId, String shopHost, String shopRequestUrl, String shopSecret,
                              String shopwareVersion) {
        this.registrationRequestedAt = OffsetDateTime.now();
        this.appKey = appKey;
        this.shopId = shopId;
        this.shopHost = shopHost;
        this.shopRequestUrl = shopRequestUrl;
        this.shopSecret = shopSecret;

        this.shopwareVersion = shopwareVersion;
        this.shopwareVersionLastUpdatedAt = OffsetDateTime.now();
    }

    public void confirmRegistrationAndAddShopApiSecrets(String apiKey, String apiSecretKey) {
        registrationConfirmedAt = OffsetDateTime.now();
        shopAdminApiKey = apiKey;
        shopAdminApiSecretKey = apiSecretKey;
        registrationConfirmed = true;
    }

    public void revertConfirmation() {
        registrationConfirmedAt = null;
        shopAdminApiKey = null;
        shopAdminApiSecretKey = null;
        registrationConfirmed = false;
    }

    public boolean isMarkedAsDeleted() {
        return deletedAt != null;
    }

    public void markAsDeleted() {
        if (this.deletedAt == null) {
            this.deletedAt = LocalDateTime.now();
        }
    }

    public void revertDeletion() {
        this.deletedAt = null;
    }

    public void updateShopwareVersion(String shopwareVersion) {
        this.shopwareVersionLastUpdatedAt = OffsetDateTime.now();
        this.shopwareVersion = shopwareVersion;
    }

    /**
     * Sets the {@link #shopHost}
     */
    public void setShopHost(String shopHost) {
        this.shopHost = shopHost;
    }

    /**
     * Sets the {@link #shopRequestUrl}
     */
    public void setShopRequestUrl(String shopRequestUrl) {
        this.shopRequestUrl = shopRequestUrl;
    }

    /**
     * Sets the {@link #shopSecret}
     */
    public void setShopSecret(String shopSecret) {
        this.shopSecret = shopSecret;
    }

    /**
     * Sets the {@link #appVersionLastUpdatedAt}
     */
    public void setAppVersionLastUpdatedAt(OffsetDateTime appVersionLastUpdatedAt) {
        this.appVersionLastUpdatedAt = appVersionLastUpdatedAt;
    }

    /**
     * Sets the {@link #shopwareVersionLastUpdatedAt}
     */
    public void setShopwareVersionLastUpdatedAt(OffsetDateTime shopwareVersionLastUpdatedAt) {
        this.shopwareVersionLastUpdatedAt = shopwareVersionLastUpdatedAt;
    }

    /**
     * Sets the {@link #lastUsedAt}
     */
    public void setLastUsedAt(OffsetDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    /**
     * Gets the {@link #appKey}.
     */
    public String getAppKey() {
        return appKey;
    }

    /**
     * Gets the {@link #id}.
     */
    public long getId() {
        return id;
    }

    /**
     * Gets the {@link #shopId}.
     */
    public String getShopId() {
        return shopId;
    }

    /**
     * Gets the {@link #shopHost}.
     */
    public String getShopHost() {
        return shopHost;
    }

    /**
     * Gets the {@link #shopRequestUrl}.
     */
    public String getShopRequestUrl() {
        return shopRequestUrl;
    }

    /**
     * Gets the {@link #shopSecret}.
     */
    public String getShopSecret() {
        return shopSecret;
    }

    /**
     * Gets the {@link #registrationRequestedAt}.
     */
    public OffsetDateTime getRegistrationRequestedAt() {
        return registrationRequestedAt;
    }

    /**
     * Gets the {@link #registrationConfirmed}.
     */
    public boolean isRegistrationConfirmed() {
        return registrationConfirmed;
    }

    /**
     * Gets the {@link #shopAdminApiKey}.
     */
    public String getShopAdminApiKey() {
        return shopAdminApiKey;
    }

    /**
     * Gets the {@link #shopAdminApiSecretKey}.
     */
    public String getShopAdminApiSecretKey() {
        return shopAdminApiSecretKey;
    }

    /**
     * Gets the {@link #registrationConfirmedAt}.
     */
    public OffsetDateTime getRegistrationConfirmedAt() {
        return registrationConfirmedAt;
    }

    /**
     * Gets the {@link #appVersion}.
     */
    public String getAppVersion() {
        return appVersion;
    }

    /**
     * Gets the {@link #appVersionLastUpdatedAt}.
     */
    public OffsetDateTime getAppVersionLastUpdatedAt() {
        return appVersionLastUpdatedAt;
    }

    /**
     * Gets the {@link #shopwareVersion}.
     */
    public String getShopwareVersion() {
        return shopwareVersion;
    }

    /**
     * Gets the {@link #shopwareVersionLastUpdatedAt}.
     */
    public OffsetDateTime getShopwareVersionLastUpdatedAt() {
        return shopwareVersionLastUpdatedAt;
    }

    /**
     * Gets the {@link #lastUsedAt}.
     */
    public OffsetDateTime getLastUsedAt() {
        return lastUsedAt;
    }

    /**
     * Gets the {@link #deletedAt}.
     */
    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
}
