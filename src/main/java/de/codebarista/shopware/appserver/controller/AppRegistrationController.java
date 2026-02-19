package de.codebarista.shopware.appserver.controller;

import de.codebarista.shopware.appserver.api.ApiConstants;
import de.codebarista.shopware.appserver.api.dto.registration.ShopwareAppConfirmationDto;
import de.codebarista.shopware.appserver.api.dto.registration.ShopwareAppRegistrationResponseDto;
import de.codebarista.shopware.appserver.config.AppServerProperties;
import de.codebarista.shopware.appserver.model.ShopwareShopEntity;
import de.codebarista.shopware.appserver.service.AppLookupService;
import de.codebarista.shopware.appserver.service.ShopManagementService;
import de.codebarista.shopware.appserver.ShopwareApp;
import de.codebarista.shopware.appserver.service.SignatureService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/shopware/api/v1/registration")
public class AppRegistrationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppRegistrationController.class);
    private static final String CONFIRMATION_URL = "/shopware/api/v1/registration/confirm";
    private final ShopManagementService shopManagementService;
    private final SignatureService signatureService;
    private final AppLookupService appLookupService;
    private final AppServerProperties appServerProperties;

    public AppRegistrationController(ShopManagementService shopManagementService,
                                     SignatureService signatureService,
                                     AppLookupService appLookupService,
                                     AppServerProperties appServerProperties) {
        this.shopManagementService = shopManagementService;
        this.signatureService = signatureService;
        this.appLookupService = appLookupService;
        this.appServerProperties = appServerProperties;
    }

    @GetMapping("/register")
    ResponseEntity<ShopwareAppRegistrationResponseDto> register(
            HttpServletRequest request,
            @RequestHeader(HttpHeaders.HOST) String host,
            @RequestHeader(ApiConstants.SHOPWARE_APP_SIGNATURE_HEADER) String appSignature,
            @Nullable @RequestHeader(value = ApiConstants.SHOPWARE_SHOP_SIGNATURE_HEADER, required = false) String shopSignature,
            @RequestParam("shop-id") String shopId,
            @RequestParam("shop-url") String shopUrl,
            @RequestParam String timestamp,
            @Nullable @RequestHeader(value = "sw-version", required = false) String shopwareVersion // Shopware >= 6.4.1.0
    ) {
        if (shopId.isBlank() || shopUrl.isBlank() || timestamp.isBlank() || appSignature.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        final ShopwareApp app = appLookupService.getAppForHost(host);
        final byte[] validationQuery = String.format("shop-id=%s&shop-url=%s&timestamp=%s", shopId, shopUrl, timestamp).getBytes(StandardCharsets.UTF_8);

        // Verify app signature
        if (!signatureService.verifySignature(validationQuery, app.getAppSecret(), appSignature)) {
            LOGGER.atError().setMessage("Registration failed: Invalid app signature")
                .addKeyValue("app", app.getAppName())
                .addKeyValue("shop-id", shopId)
                .addKeyValue("shop-url", shopUrl)
                .log();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean shopSignatureVerified = false;
        ShopwareShopEntity existingShop = shopManagementService.getShopById(app, shopId).orElse(null);
        // Verify shop signature on re-registration only if existing shop is confirmed
        if (existingShop != null && existingShop.isRegistrationConfirmed()) {
            if (shopSignature != null) {
                shopSignatureVerified = signatureService.verifySignature(validationQuery, existingShop.getShopSecret(), shopSignature);
                if (!shopSignatureVerified) {
                    LOGGER.atError().setMessage("Registration failed: Invalid shop signature")
                        .addKeyValue("app", app.getAppName())
                        .addKeyValue("shop-id", shopId)
                        .addKeyValue("shop-url", shopUrl)
                        .log();
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                }
            } else if (existingShop.reRegistrationRequiresShopSignature() || appServerProperties.isReRegistrationWithShopSignatureEnforced()) {
                LOGGER.atError().setMessage("Registration failed: Missing required shop signature")
                    .addKeyValue("app", app.getAppName())
                    .addKeyValue("shop-id", shopId)
                    .addKeyValue("shop-url", shopUrl)
                    .log();
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            // else: The shop signature is missing but neither the shop nor the global configuration
            // requires it. A shop requires it once it has previously re-registered with a verified shop
            // signature. This is allowed for backwards compatibility with Shopware versions that do not
            // send a shop signature on re-registration.
        }

        LOGGER.info("App installation in progress: {}, URL: {}, ID: {}", app, shopUrl, shopId);

        String shopSecret = shopManagementService.registerShop(app, shopId, shopUrl, shopwareVersion, shopSignatureVerified);
        String proof = calculateProof(app, shopId, shopUrl);
        String confirmationUrl = buildConfirmationUrl(request);
        return ResponseEntity.ok(ShopwareAppRegistrationResponseDto.success(proof, shopSecret, confirmationUrl));
    }

    private String calculateProof(ShopwareApp app, String shopId, String shopUrl) {
        String secret = app.getAppSecret();
        String dataToSign = shopId + shopUrl + app.getAppName();
        return signatureService.calculateSignature(dataToSign, secret);
    }

    private String buildConfirmationUrl(HttpServletRequest servletRequest) {
        // allow url with HTTP scheme only, if sslOnly is false
        boolean requestSchemeWithoutSsl = servletRequest.getScheme().equals("http");
        String scheme = "https";
        if (requestSchemeWithoutSsl && !appServerProperties.isSslOnly()) {
            scheme = "http";
        }

        return ServletUriComponentsBuilder.fromRequestUri(servletRequest)
                .replacePath(null)
                .scheme(scheme)
                .build()
                .toUriString()
                + CONFIRMATION_URL;
    }

    @PostMapping("/confirm")
    ResponseEntity<Void> confirm(
            @RequestHeader(HttpHeaders.HOST) String host,
            @RequestBody ShopwareAppConfirmationDto confirmation) {
        var app = appLookupService.getAppForHost(host);
        boolean confirmed = shopManagementService.confirmShopRegistration(
                app, confirmation.getShopId(), confirmation.getShopUrl(),
                confirmation.getApiKey(), confirmation.getSecretKey());
        if (confirmed) {
            LOGGER.info("App {} installation confirmed. URL: {}, ID: {}",
                    app, confirmation.getShopUrl(), confirmation.getShopId());
            return ResponseEntity.accepted().build();
        } else {
            LOGGER.warn("App {} installation aborted. URL: {}, ID: {}",
                    app, confirmation.getShopUrl(), confirmation.getShopId());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
