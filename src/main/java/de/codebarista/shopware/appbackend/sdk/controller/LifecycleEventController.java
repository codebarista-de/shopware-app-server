package de.codebarista.shopware.appbackend.sdk.controller;

import de.codebarista.shopware.appbackend.sdk.api.dto.lifecycle.ShopwareAppLifecycleEventDto;
import de.codebarista.shopware.appbackend.sdk.service.AppLookupService;
import de.codebarista.shopware.appbackend.sdk.service.ShopManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/shopware/api/v1/lifecycle")
public class LifecycleEventController {
    private static final Logger LOGGER = LoggerFactory.getLogger(LifecycleEventController.class);
    private final ShopManagementService shopManagementService;
    private final AppLookupService appLookupService;

    public LifecycleEventController(ShopManagementService shopManagementService, AppLookupService appLookupService) {
        this.shopManagementService = shopManagementService;
        this.appLookupService = appLookupService;
    }

    @PostMapping("/updated")
    ResponseEntity<Void> lifecycleUpdated(
            @RequestHeader(HttpHeaders.HOST) String host,
            @RequestBody ShopwareAppLifecycleEventDto eventDto) {
        var app = appLookupService.getAppForHost(host);
        var source = eventDto.source();
        // the event is triggered by the updated app, it sends us the new version
        LOGGER.info("App {} updated to version {}. URL: {}, ID: {}",
                app, eventDto.source().appVersion(), source.shopUrl(), source.shopId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/deleted")
    ResponseEntity<Void> lifecycleDeleted(
            @RequestHeader(HttpHeaders.HOST) String host,
            @RequestBody ShopwareAppLifecycleEventDto eventDto) {
        var app = appLookupService.getAppForHost(host);
        var source = eventDto.source();
        shopManagementService.deleteShop(app, source.shopId(), source.shopUrl());
        LOGGER.info("App {} deleted. URL: {}, ID: {}", app, source.shopUrl(), source.shopId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/activated")
    ResponseEntity<Void> lifecycleActivated(
            @RequestHeader(HttpHeaders.HOST) String host,
            @RequestBody ShopwareAppLifecycleEventDto eventDto) {
        var app = appLookupService.getAppForHost(host);
        var source = eventDto.source();
        LOGGER.info("App {} activated. URL: {}, ID: {}", app, source.shopUrl(), source.shopId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/deactivated")
    ResponseEntity<Void> lifecycleDeactivated(
            @RequestHeader(HttpHeaders.HOST) String host,
            @RequestBody ShopwareAppLifecycleEventDto eventDto) {
        var app = appLookupService.getAppForHost(host);
        var source = eventDto.source();
        LOGGER.info("App {} deactivated. URL: {}, ID: {}", app, source.shopUrl(), source.shopId());
        return ResponseEntity.noContent().build();
    }
}
