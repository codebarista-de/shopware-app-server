package com.example.shopwareapp;

import de.codebarista.shopware.appserver.AdminApi;
import de.codebarista.shopware.appserver.ShopwareApp;
import de.codebarista.shopware.appserver.api.dto.action.ActionRequestDto;
import de.codebarista.shopware.appserver.api.dto.action.ActionResponseDto;
import de.codebarista.shopware.appserver.api.dto.event.ShopwareEventDto;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Example Shopware App implementation demonstrating:
 * - App configuration
 * - Webhook event handling
 * - Action button handling
 * - Admin API integration
 */
@Component
public class MyShopwareApp implements ShopwareApp {
    private static final Logger log = LoggerFactory.getLogger(MyShopwareApp.class);

    private final AdminApi adminApi;

    public MyShopwareApp(AdminApi adminApi) {
        this.adminApi = adminApi;
    }

    @Override
    public String getAppKey() {
        // Must match the subdomain where this app is hosted
        // Example: my-app.example.com -> app key is "my-app"
        return "my-app";
    }

    @Override
    public String getAppSecret() {
        // The shared secret from manifest.xml
        // In production: load from environment variable or secure configuration
        return "my-app-secret";
    }

    @Override
    public String getAppName() {
        return "MyShopwareApp";
    }

    /**
     * Handle webhook events from Shopware.
     * Events are configured in manifest.xml.
     */
    @Override
    public void onEvent(ShopwareEventDto event, long internalShopId, @Nullable Locale userLocale, String shopwareLanguageId) {
        String eventName = event.data().event();
        String shopId = event.source().shopId();

        log.info("Received event '{}' from shop {}", eventName, shopId);

        switch (eventName) {
            case "order.written" -> handleOrderWritten(event, shopId);
            case "product.written" -> handleProductWritten(event, shopId);
            default -> log.warn("Unhandled event: {}", eventName);
        }
    }

    /**
     * Handle action button clicks from Shopware Administration.
     * Action buttons are configured in manifest.xml.
     */
    @Override
    public ActionResponseDto<?> onAction(ActionRequestDto action, long internalShopId, @Nullable Locale userLocale, String shopwareLanguageId) {
        String actionName = action.data().action();
        String shopId = action.source().shopId();

        log.info("Received action '{}' from shop {}", actionName, shopId);

        return switch (actionName) {
            case "process_order" -> processOrder(action, shopId);
            case "sync_product" -> syncProduct(action, shopId);
            default -> ActionResponseDto.errorNotification("Unknown action: " + actionName);
        };
    }

    // === Event Handlers ===

    private void handleOrderWritten(ShopwareEventDto event, String shopId) {
        log.info("Order written: {}", event.data().payload());

        // Example: Send success notification to admin
        adminApi.pushSuccessMessage(this, shopId, "Order received and processing started!");

        // Example: Process the order data
        // var orderData = event.data().payload();
        // processOrderData(orderData);
    }

    private void handleProductWritten(ShopwareEventDto event, String shopId) {
        log.info("Product written: {}", event.data().payload());

        // Example: Sync product to external system
        // var productData = event.data().payload();
        // syncToExternalSystem(productData);
    }

    // === Action Handlers ===

    private ActionResponseDto<?> processOrder(ActionRequestDto action, String shopId) {
        log.info("Processing order via action button");

        try {
            // Example: Extract order ID from action data
            // var orderId = action.data().ids().get(0);

            // Example: Process the order
            // processOrderLogic(orderId);

            // Send success notification
            adminApi.pushSuccessMessage(this, shopId, "Order processed successfully!");

            return ActionResponseDto.successNotification("Order has been processed");
        } catch (Exception e) {
            log.error("Failed to process order", e);

            adminApi.pushErrorMessage(this, shopId, "Failed to process order: " + e.getMessage());

            return ActionResponseDto.errorNotification("Processing failed: " + e.getMessage());
        }
    }

    private ActionResponseDto<?> syncProduct(ActionRequestDto action, String shopId) {
        log.info("Syncing product via action button");

        // Example: Get product IDs from action
        var productIds = action.data().ids();
        log.info("Syncing {} products", productIds.size());

        // Example: Use Admin API to fetch product data
        // for (String productId : productIds) {
        //     var searchQuery = new SearchQuery();
        //     searchQuery.setIds(List.of(productId));
        //     var result = adminApi.searchEntity(this, shopId, "product", searchQuery, ProductDto.class, shopwareLanguageId);
        //     // Process product data...
        // }

        adminApi.pushInfoMessage(this, shopId, "Product sync completed for " + productIds.size() + " products");

        return ActionResponseDto.infoNotification("Products synced successfully");
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public String getAdminExtensionFolderName() {
        return null;
    }

    @Override
    public void onRegisterShop(@Nonnull String shopHost, @Nonnull String shopId, long internalShopId) {

    }

    @Override
    public void onReRegisterShop(@Nonnull String shopHost, @Nonnull String shopId, long internalShopId) {

    }

    @Override
    public void onDeleteShop(@Nonnull String shopHost, @Nonnull String shopId, long internalShopId) {

    }
}
