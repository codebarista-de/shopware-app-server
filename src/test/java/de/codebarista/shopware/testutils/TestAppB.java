package de.codebarista.shopware.testutils;

import de.codebarista.shopware.appbackend.sdk.api.dto.action.ActionRequestDto;
import de.codebarista.shopware.appbackend.sdk.api.dto.action.ActionResponseDto;
import de.codebarista.shopware.appbackend.sdk.api.dto.event.ShopwareEventDto;
import de.codebarista.shopware.appbackend.sdk.service.ShopwareApp;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class TestAppB implements ShopwareApp {
    public static String APP_NAME = "TestAppB";
    public static String APP_KEY = "test-b";
    public static String ADMIN_EXT_FOLDER = "test-app-b";

    @Override
    public String getAppKey() {
        return APP_KEY;
    }

    @Override
    public String getAppSecret() {
        return "mysecret";
    }

    @Override
    public String getAppName() {
        return APP_NAME;
    }

    @Nullable
    @Override
    public String getVersion() {
        return "";
    }

    @Override
    public String getAdminExtensionFolderName() {
        return ADMIN_EXT_FOLDER;
    }

    @Override
    public String toString() {
        return getAppKey();
    }

    @Override
    public void onRegisterShop(String shopHost, String shopId, long internalShopId) {

    }

    @Override
    public void onReRegisterShop(String shopHost, String shopId, long internalShopId) {

    }

    @Override
    public void onDeleteShop(String shopHost, String shopId, long internalShopId) {

    }

    @Override
    public void onEvent(ShopwareEventDto event, long internalShopId, @Nullable Locale userLocale, String shopwareLanguageId) {

    }

    @Override
    public ActionResponseDto<?> onAction(ActionRequestDto action, long internalShopId, @Nullable Locale userLocale, String shopwareLanguageId) {
        return null;
    }
}
