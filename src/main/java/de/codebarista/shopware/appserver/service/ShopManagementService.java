package de.codebarista.shopware.appserver.service;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import de.codebarista.shopware.appserver.ShopwareApp;
import de.codebarista.shopware.appserver.config.AppServerProperties;
import de.codebarista.shopware.appserver.exception.InvalidShopUrlException;
import de.codebarista.shopware.appserver.exception.NoSuchShopException;
import de.codebarista.shopware.appserver.model.ShopwareShopEntity;
import de.codebarista.shopware.appserver.model.ShopwareShopEntityRepository;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing shop registrations and lifecycle.
 * <p>
 * This service is automatically configured by {@link de.codebarista.shopware.appserver.config.AppServerServiceAutoConfiguration}.
 * Users can override it by defining their own {@code ShopManagementService} bean.
 */
public class ShopManagementService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShopManagementService.class);

    public static final char[] SHOP_SECRET_ALPHABET =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    private final ShopwareShopEntityRepository shopwareShopEntityRepository;
    private final AppServerProperties properties;

    public ShopManagementService(ShopwareShopEntityRepository shopwareShopEntityRepository,
                                 AppServerProperties properties) {
        this.shopwareShopEntityRepository = shopwareShopEntityRepository;
        this.properties = properties;
    }

    @Nonnull public String registerShop(@Nonnull ShopwareApp app, @Nonnull String shopId, @Nonnull String shopUrl, @Nonnull String shopwareVersion) {
        return getShopById(app, shopId)
                .map(shop -> reRegisterExistingShop(app, shop, shopId, shopUrl, shopwareVersion))
                .orElseGet(() -> registerNewShop(app, shopId, shopUrl, shopwareVersion));
    }

    private String registerNewShop(ShopwareApp app, String shopId, String shopUrl, String shopwareVersion) {
        LOGGER.info("Registering new shop for app {} with ID '{}'", app, shopId);
        String shopSecret = generateShopSecret();
        String shopHost = inferShopHost(shopUrl);
        final var shop = shopwareShopEntityRepository.save(new ShopwareShopEntity(app.getAppKey(), shopId, shopHost, shopUrl, shopSecret, shopwareVersion));
        app.onRegisterShop(shopHost, shopId, shop.getId());
        return shopSecret;
    }

    private String reRegisterExistingShop(ShopwareApp app, ShopwareShopEntity shop, String shopId, String shopUrl, String shopwareVersion) {
        LOGGER.info("Re-registering existing shop for app {} with ID '{}'", shop.getAppKey(), shop.getShopId());

        String shopHost = inferShopHost(shopUrl);
        String newSecret = generateShopSecret();
        shop.setShopHost(shopHost);
        shop.setShopRequestUrl(shopUrl);
        shop.setShopSecret(newSecret);
        shop.updateShopwareVersion(shopwareVersion);
        shop.revertConfirmation();
        shop.revertDeletion();
        shop = shopwareShopEntityRepository.save(shop);

        app.onReRegisterShop(shopHost, shopId, shop.getId());

        return newSecret;
    }

    private String inferShopHost(String shopUrl) {
        try {
            String host = new URI(shopUrl).getHost();
            if (properties.isMapLocalhostIPToLocalhostDomainName() && "127.0.0.1".equals(host)) {
                LOGGER.info("Mapping 127.0.0.1 to localhost");
                host = "localhost";
            }
            LOGGER.debug("Get shop host for url '{}': {}", shopUrl, host);
            return host;
        } catch (URISyntaxException e) {
            throw new InvalidShopUrlException("Cannot infer host from shop url '" + shopUrl + "': " + e.getMessage());
        }
    }

    public boolean confirmShopRegistration(
            @Nonnull ShopwareApp app, @Nonnull String shopId, @Nonnull String shopUrl, @Nonnull String apiKey, @Nonnull String secretKey) {
        String shopHost = inferShopHost(shopUrl);
        return shopwareShopEntityRepository.findByAppKeyAndShopId(app.getAppKey(), shopId)
                .map(shopEntity -> confirmRegistration(shopEntity, shopHost, apiKey, secretKey))
                .orElse(false);
    }

    private boolean confirmRegistration(ShopwareShopEntity shop, String confirmShopHost,
                                        String apiKey, String secretKey) {
        if (!shop.getShopHost().equals(confirmShopHost)) {
            LOGGER.warn("Shop registration failed: Shop hosts do not match. Known host: '{}', " +
                    "host from confirmation request: '{}'", shop.getShopHost(), confirmShopHost);
            return false;
        }
        shop.confirmRegistrationAndAddShopApiSecrets(apiKey, secretKey);
        shopwareShopEntityRepository.save(shop);
        return true;
    }

    @Nonnull public Optional<ShopwareShopEntity> getShopByUrl(@Nonnull ShopwareApp app, @Nonnull String shopUrl) {
        String shopHost = inferShopHost(shopUrl);
        LOGGER.debug("Get shop for {} by host '{}'", app, shopHost);
        var shops = shopwareShopEntityRepository.findByAppKeyAndShopHost(app.getAppKey(), shopHost);
        if (shops.isEmpty()) {
            return Optional.empty();
        } else if (shops.size() == 1) {
            return Optional.of(shops.get(0));
        } else {
            String candidates = shops.stream()
                    .map(shop -> shop.getShopId() + ": " + shop.getShopRequestUrl())
                    .collect(Collectors.joining("\n"));
            LOGGER.error("Cannot clearly identify shop for {} by shop host '{}' (URL: {})\nCandidates: {}",
                    app.getAppKey(), shopHost, shopUrl, candidates);
            return Optional.empty();
        }
    }

    @Nonnull public Optional<ShopwareShopEntity> getShopById(@Nonnull ShopwareApp app, @Nonnull String shopId) {
        LOGGER.debug("Get shop for {} by id '{}'", app, shopId);
        return shopwareShopEntityRepository.findByAppKeyAndShopId(app.getAppKey(), shopId);
    }

    /**
     * @throws NoSuchShopException if no shop with the specified ID exists for this app
     */
    @Nonnull public ShopwareShopEntity getShopByIdOrThrow(@Nonnull ShopwareApp app, @Nonnull String shopId) {
        LOGGER.debug("Get shop for {} by id '{}'", app, shopId);
        return shopwareShopEntityRepository.findByAppKeyAndShopId(app.getAppKey(), shopId)
                .orElseThrow(() -> NoSuchShopException.byId(app, shopId));
    }

    public void deleteShop(@Nonnull ShopwareApp app, @Nonnull String shopId, @Nonnull String shopUrl) {
        String shopHost = inferShopHost(shopUrl);

        final var shop = shopwareShopEntityRepository.findByAppKeyAndShopId(app.getAppKey(), shopId).orElse(null);
        if (shop == null) {
            return;
        }
        shop.markAsDeleted();
        shopwareShopEntityRepository.save(shop);

        app.onDeleteShop(shopHost, shopId, shop.getId());

        LOGGER.info("Marked {} for shop {} ({}) as deleted", app, shopId, shopHost);
    }

    String generateShopSecret() {
        return NanoIdUtils.randomNanoId(NanoIdUtils.DEFAULT_NUMBER_GENERATOR, SHOP_SECRET_ALPHABET, 64);
    }
}
