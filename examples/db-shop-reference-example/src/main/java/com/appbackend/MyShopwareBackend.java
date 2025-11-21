package com.appbackend;

import com.appbackend.entity.EventLog;
import com.appbackend.repository.EventLogRepository;
import de.codebarista.shopware.appserver.ShopwareApp;
import de.codebarista.shopware.appserver.api.dto.action.ActionRequestDto;
import de.codebarista.shopware.appserver.api.dto.action.ActionResponseDto;
import de.codebarista.shopware.appserver.api.dto.event.ShopwareEventDto;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Component
public class MyShopwareBackend implements ShopwareApp {

    private static final Logger log = LoggerFactory.getLogger(MyShopwareBackend.class);

    private final EventLogRepository eventLogRepository;

    public MyShopwareBackend(EventLogRepository eventLogRepository) {
        this.eventLogRepository = eventLogRepository;
    }

    @Override
    public String getAppKey() {
        // Must match the subdomain where this app is hosted
        // Example: my-app.example.com -> app key is "my-app"
        return "my-db-app";
    }

    @Override
    public String getAppName() {
        return "DatabaseExampleApp";
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

        // Log the shop registration event
        EventLog event = new EventLog(
                internalShopId,
                "shop.registered",
                LocalDateTime.now()
        );
        eventLogRepository.save(event);
        log.info("Logged registration event for shop ID: {}", internalShopId);
    }

    @Override
    public void onReRegisterShop(@Nonnull String shopHost, @Nonnull String shopId, long internalShopId) {
        log.info("Shop re-registered: {} (ID: {})", shopHost, shopId);

        // Log the re-registration event
        EventLog event = new EventLog(
                internalShopId,
                "shop.re-registered",
                LocalDateTime.now()
        );
        eventLogRepository.save(event);
    }

    @Override
    public void onDeleteShop(@Nonnull String shopHost, @Nonnull String shopId, long internalShopId) {
    }

    @Override
    public void onEvent(@Nonnull ShopwareEventDto event, long internalShopId,
                        @Nullable Locale userLocale, @Nullable String shopwareLanguageId) {
        String eventName = event.data().event();
        log.info("Received event '{}' from shop ID: {}", eventName, internalShopId);

        // Log the Shopware event
        EventLog eventLog = new EventLog(
                internalShopId,
                eventName,
                LocalDateTime.now()
        );
        eventLogRepository.save(eventLog);

        // Query recent events for this shop
        List<EventLog> recentEvents = eventLogRepository
                .findByShopIdOrderByReceivedAtDesc(internalShopId);
        log.info("Total events logged for shop ID {}: {}", internalShopId, recentEvents.size());
    }

    @Override
    public ActionResponseDto<?> onAction(@Nonnull ActionRequestDto action, long internalShopId,
                                         @Nullable Locale userLocale, @Nullable String shopwareLanguageId) {
        return null;
    }
}
