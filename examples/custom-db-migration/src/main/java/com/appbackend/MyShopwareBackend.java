package com.appbackend;

import de.codebarista.shopware.appserver.ShopwareApp;
import de.codebarista.shopware.appserver.api.dto.action.ActionRequestDto;
import de.codebarista.shopware.appserver.api.dto.action.ActionResponseDto;
import de.codebarista.shopware.appserver.api.dto.event.ShopwareEventDto;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class MyShopwareBackend implements ShopwareApp {
    @Override
    public String getAppKey() {
        // Must match the subdomain where this app is hosted
        // Example: my-app.example.com -> app key is "my-app"
        return "my-app";
    }

    @Override
    public String getAppName() {
        return "MyShopwareAppWithCustomDbEntity";
    }

    @Override
    public String getAppSecret() {
        return "";
    }


    @Nullable
    @Override
    public String getVersion() {
        return null;
    }

    @Nullable
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

    @Override
    public void onEvent(@Nonnull ShopwareEventDto event, long internalShopId, @Nullable Locale userLocale, @Nullable String shopwareLanguageId) {

    }

    @Nonnull
    @Override
    public ActionResponseDto<?> onAction(@Nonnull ActionRequestDto action, long internalShopId, @Nullable Locale userLocale, @Nullable String shopwareLanguageId) {
        return null;
    }
}
