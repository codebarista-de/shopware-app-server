package de.codebarista.shopware.appserver.controller;

import de.codebarista.shopware.appserver.service.AppLookupService;
import de.codebarista.shopware.appserver.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

@Controller
public class AdminExtensionController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminExtensionController.class);

    private final AppLookupService appLookupService;
    private final TokenService tokenService;

    public AdminExtensionController(AppLookupService appLookupService,
                                    TokenService tokenService) {
        this.appLookupService = appLookupService;
        this.tokenService = tokenService;
    }

    @GetMapping("/shopware/admin/{admin-extension-folder}/{version}/index.html")
    @ResponseBody
    ResponseEntity<String> getAdminExtension(
            @RequestHeader(HttpHeaders.HOST) String host,
            @PathVariable("admin-extension-folder") String adminExtensionFolder,
            @PathVariable("version") String version,
            @RequestParam("shop-id") String shopId
    ) {
        var app = appLookupService.getAppForHost(host);
        if (!app.getAdminExtensionFolderName().equals(adminExtensionFolder)) {
            LOGGER.warn("Admin extension folder {} requested by app {} whose admin extension folder should be {}",
                    adminExtensionFolder,
                    app,
                    app.getAdminExtensionFolderName());
            throw new AccessDeniedException("Invalid admin extension folder.");
        }
        String resourcePath = Paths.get(
                "/public/shopware/admin",
                app.getAdminExtensionFolderName(),
                version,
                "index.html").toString();
        Resource resource = new ClassPathResource(resourcePath);
        try {
            String html = resource.getContentAsString(StandardCharsets.UTF_8);

            String appToken = tokenService.generateAppToken(app, shopId);
            html = html.replace("data-token=\"\"", String.format("data-token=\"%s\"", appToken));

            String appVersion = app.getVersion();
            if (appVersion != null) {
                html = html.replace("data-version=\"\"", String.format("data-version=\"%s\"", appVersion));
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE)
                    .cacheControl(CacheControl.noStore())
                    .header(HttpHeaders.PRAGMA, "no-cache")
                    .header(HttpHeaders.EXPIRES, "0")
                    .body(html);
        } catch (IOException e) {
            LOGGER.error("Failed to load admin extension {}", resourcePath, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
