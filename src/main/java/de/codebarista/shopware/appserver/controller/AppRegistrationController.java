package de.codebarista.shopware.appserver.controller;

import de.codebarista.shopware.appserver.api.dto.registration.ShopwareAppConfirmationDto;
import de.codebarista.shopware.appserver.api.dto.registration.ShopwareAppRegistrationResponseDto;
import de.codebarista.shopware.appserver.config.AppServerProperties;
import de.codebarista.shopware.appserver.service.AppLookupService;
import de.codebarista.shopware.appserver.service.ShopManagementService;
import de.codebarista.shopware.appserver.service.ShopwareApp;
import de.codebarista.shopware.appserver.service.SignatureService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/shopware/api/v1/registration")
public class AppRegistrationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppRegistrationController.class);
    public final String CONFIRMATION_URL = "/shopware/api/v1/registration/confirm";
    private final ShopManagementService shopManagementService;
    private final SignatureService signatureService;
    private final AppLookupService appLookupService;
    private final boolean sslOnly;

    public AppRegistrationController(ShopManagementService shopManagementService,
                                     SignatureService signatureService,
                                     AppLookupService appLookupService,
                                     AppServerProperties sdkProperties) {
        this.shopManagementService = shopManagementService;
        this.signatureService = signatureService;
        this.appLookupService = appLookupService;
        this.sslOnly = sdkProperties.isSslOnly();
    }

    @GetMapping("/register")
    ResponseEntity<ShopwareAppRegistrationResponseDto> register(
            HttpServletRequest servletRequest,
            @RequestHeader(HttpHeaders.HOST) String host,
            @RequestParam("shop-id") String shopId,
            @RequestParam("shop-url") String shopUrl,
            @RequestParam("timestamp") Long timestamp,
            @Nullable @RequestHeader(value = "sw-version", required = false) String shopwareVersion // Shopware >= 6.4.1.0
    ) {

        ShopwareApp app = appLookupService.getAppForHost(host);
        LOGGER.info("App installation in progress: {}, URL: {}, ID: {}", app, shopUrl, shopId);

        if (shopId.isBlank() || shopUrl.isBlank()) {
            throw new AccessDeniedException("Shop ID and Shop URL must not be blank.");
        }

        String shopSecret = shopManagementService.registerShop(app, shopId, shopUrl, shopwareVersion);
        String proof = calculateProof(app, shopId, shopUrl);
        String confirmationUrl = buildConfirmationUrl(servletRequest);
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
        if (requestSchemeWithoutSsl && !sslOnly) {
            scheme = "http";
        }

        // TODO: make template
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
            return ResponseEntity.badRequest().build();
        }
    }
}
