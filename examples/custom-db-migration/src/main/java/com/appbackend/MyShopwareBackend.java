package com.appbackend;

import com.appbackend.entity.SystemMessage;
import com.appbackend.repository.SystemMessageRepository;
import de.codebarista.shopware.appserver.ShopwareApp;
import de.codebarista.shopware.appserver.api.dto.action.ActionRequestDto;
import de.codebarista.shopware.appserver.api.dto.action.ActionResponseDto;
import de.codebarista.shopware.appserver.api.dto.event.ShopwareEventDto;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class MyShopwareBackend implements ShopwareApp {

    private static final Logger log = LoggerFactory.getLogger(MyShopwareBackend.class);

    private final SystemMessageRepository systemMessageRepository;

    public MyShopwareBackend(SystemMessageRepository systemMessageRepository) {
        this.systemMessageRepository = systemMessageRepository;
    }

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
        return "my-app-secret";
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
        log.info("Shop registered: {} (ID: {})", shopHost, shopId);

        // Example: Create a system message when a shop registers
        SystemMessage message = new SystemMessage(
                "System is operational. Shop " + shopHost + " successfully registered.",
                "System",
                false
        );
        systemMessageRepository.save(message);
        log.info("Saved system message: {}", message);
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
