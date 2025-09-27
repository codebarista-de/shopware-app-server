package de.codebarista.shopware.appserver.controller;

import de.codebarista.shopware.appserver.api.ApiConstants;
import de.codebarista.shopware.appserver.api.dto.action.ActionRequestDto;
import de.codebarista.shopware.appserver.api.dto.action.ActionResponseDto;
import de.codebarista.shopware.appserver.api.dto.event.ShopwareEventDto;
import de.codebarista.shopware.appserver.service.AppLookupService;
import de.codebarista.shopware.appserver.service.ShopwareApp;
import de.codebarista.shopware.appserver.service.SignatureService;
import de.codebarista.shopware.appserver.model.ShopwareShopEntityRepository;
import de.codebarista.shopware.appserver.util.Locales;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shopware/api/v1")
public class ShopwareController {
    private final AppLookupService appLookupService;
    private final ShopwareShopEntityRepository shopwareShopEntityRepository;
    private final SignatureService signatureService;

    public ShopwareController(AppLookupService appLookupService,
                              ShopwareShopEntityRepository shopwareShopEntityRepository,
                              SignatureService signatureService) {
        this.appLookupService = appLookupService;
        this.shopwareShopEntityRepository = shopwareShopEntityRepository;
        this.signatureService = signatureService;
    }

    @PostMapping("/event")
    ResponseEntity<Void> webHookEvent(
            @RequestHeader(HttpHeaders.HOST) String host,
            @RequestHeader(value = ApiConstants.SHOPWARE_USER_LANGUAGE_HEADER, required = false) String shopwareUserLanguage,
            @RequestHeader(value = ApiConstants.SHOPWARE_LANGUAGE_ID_HEADER, required = false) String shopwareLanguageId,
            @RequestBody ShopwareEventDto event) {
        ShopwareApp app = appLookupService.getAppForHost(host);
        final var shop = shopwareShopEntityRepository.findByAppKeyAndShopId(app.getAppKey(), event.source().shopId()).orElse(null);
        if (shop == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        app.onEvent(event, shop.getId(), Locales.getLocale(shopwareUserLanguage), shopwareLanguageId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/action")
    ResponseEntity<String> actionEvent(
            @RequestHeader(HttpHeaders.HOST) String host,
            @RequestHeader(value = ApiConstants.SHOPWARE_USER_LANGUAGE_HEADER, required = false) String shopwareUserLanguage,
            @RequestHeader(value = ApiConstants.SHOPWARE_LANGUAGE_ID_HEADER, required = false) String shopwareLanguageId,
            @RequestBody ActionRequestDto action) {
        ShopwareApp app = appLookupService.getAppForHost(host);
        final var shop = shopwareShopEntityRepository.findByAppKeyAndShopId(app.getAppKey(), action.source().shopId())
                .orElse(null);
        if (shop == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ActionResponseDto<?> response = app.onAction(action, shop.getId(), Locales.getLocale(shopwareUserLanguage), shopwareLanguageId);
        if (response == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        SignatureService.SignedResponse signedResponse = signatureService.serializeAndCalculateSignature(response, shop.getShopSecret());

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set(ApiConstants.SHOPWARE_APP_SIGNATURE_HEADER, signedResponse.signature());
        responseHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return ResponseEntity.ok().headers(responseHeaders).body(signedResponse.response());
    }
}
