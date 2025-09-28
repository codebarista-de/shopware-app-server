package de.codebarista.shopware.appserver.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.codebarista.shopware.appserver.api.ApiConstants;
import de.codebarista.shopware.appserver.model.ShopwareShopEntity;
import de.codebarista.shopware.appserver.service.AppLookupService;
import de.codebarista.shopware.appserver.service.ShopManagementService;
import de.codebarista.shopware.appserver.ShopwareApp;
import de.codebarista.shopware.appserver.service.SignatureService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class ShopwareSignatureVerificationFilter extends OncePerRequestFilter {
    private record ShopInfo(ShopwareApp app, String shopId) {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ShopwareSignatureVerificationFilter.class);
    private final ShopManagementService shopManagementService;
    private final SignatureService signatureService;
    private final AppLookupService appLookupService;
    private final ObjectMapper objectMapper;

    public ShopwareSignatureVerificationFilter(ShopManagementService shopManagementService,
                                               SignatureService signatureService,
                                               AppLookupService appLookupService,
                                               ObjectMapper objectMapper) {
        this.shopManagementService = shopManagementService;
        this.signatureService = signatureService;
        this.appLookupService = appLookupService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (isStaticResource(request.getRequestURI())) {
            filterChain.doFilter(request, response); // Continue without processing
            return;
        }

        final String host = request.getHeader(HttpHeaders.HOST);
        final ShopwareApp app = appLookupService.tryGetForHost(host);
        if (app == null) {
            LOGGER.warn("Could not authenticate request: No app for host {}", host);
            filterChain.doFilter(request, response);
            return;
        }

        // Check for app signature first (used for registration request)
        if (isValidAppSignature(request, app)) {
            setAuthentication(app.getAppName(), AppServerWebSecurityConfiguration.ROLE_SHOPWARE_APP);
            filterChain.doFilter(request, response);
            return;
        }

        // Check for shop signature in GET request
        if (HttpMethod.GET.matches(request.getMethod())) {
            Result<ShopInfo> info = getShopInfoIfGetRequestValid(request, app);
            if (info.isError()) {
                LOGGER.warn("Shop authentication failed for GET {}. {}", request.getRequestURI(), info.getError());
            } else {
                setAuthentication(info.getResult().shopId, AppServerWebSecurityConfiguration.ROLE_SHOPWARE_SHOP);
            }
            filterChain.doFilter(request, response);
            return;
        }

        // Check for shop signature in POST request
        if (HttpMethod.POST.matches(request.getMethod())) {
            // To validate a post request the complete request body must be read.
            // Wrap the request so that the body can be read multiple times.
            var reusableRequest = new ReusableRequestWrapper(request);
            Result<ShopInfo> info = getShopInfoIfPostRequestValid(reusableRequest, app);
            if (info.isError()) {
                LOGGER.warn("Shop authentication failed for POST {}. {}", request.getRequestURI(), info.getError());
            } else {
                setAuthentication(info.getResult().shopId, AppServerWebSecurityConfiguration.ROLE_SHOPWARE_SHOP);
            }
            // Pass the wrapped request to the filter chain!
            filterChain.doFilter(reusableRequest, response);
            return;
        }

        LOGGER.warn("Shop authentication failed for {} {}. Unsupported method", request.getMethod(), request.getRequestURI());
        filterChain.doFilter(request, response);
    }

    private boolean isStaticResource(String uri) {
        // static resource file endings
        return uri.matches(".*\\.(js|css|ttf|woff|woff2|eot|svg|jpg|jpeg|png|gif|ico)$");
    }

    private boolean isValidAppSignature(HttpServletRequest request, ShopwareApp app) {
        if (!HttpMethod.GET.matches(request.getMethod())) {
            return false;
        }
        String signature = request.getHeader(ApiConstants.SHOPWARE_APP_SIGNATURE_HEADER);
        if (signature == null) {
            LOGGER.debug("App signature verification failed: Missing signature header");
            return false;
        }
        String queryWithoutSignature = request.getQueryString().replace(String.format("&%s=%s", ApiConstants.SHOPWARE_APP_SIGNATURE_HEADER, signature), "");
        if (!signatureService.verifySignature(queryWithoutSignature.getBytes(StandardCharsets.UTF_8), app.getAppSecret(), signature)) {
            LOGGER.warn("App signature verification failed: Invalid signature");
            return false;
        }
        return true;
    }

    private void setAuthentication(String principal, String authority) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority(authority)))
        );
    }

    private Result<ShopInfo> getShopInfoIfPostRequestValid(HttpServletRequest request, ShopwareApp app) throws
            IOException {
        String signature = request.getHeader(ApiConstants.SHOPWARE_SHOP_SIGNATURE_HEADER);
        byte[] body = request.getInputStream().readAllBytes();
        Result<ShopInfo> shopInfo = getShopInfoFromBody(body, app);
        if (shopInfo.isError()) {
            return shopInfo;
        }
        if (isSignatureValid(body, signature, shopInfo.getResult())) {
            return shopInfo;
        }
        return Result.error("Invalid signature for app %s", app);
    }

    private Result<ShopInfo> getShopInfoFromBody(byte[] body, ShopwareApp app) {
        try {
            JsonNode node = objectMapper.readTree(body);
            if (!node.isObject()) {
                return Result.error("Request body is not a JSON object.");
            }
            // The registration confirmation request has the shopId property directly in the root object
            JsonNode rootShopId = node.get("shopId");
            if (rootShopId != null && rootShopId.isTextual()) {
                return Result.success(new ShopInfo(app, rootShopId.textValue()));
            }
            // All other requests have the shopId in the source object
            JsonNode source = node.get("source");
            if (source != null && source.isObject()) {
                JsonNode sourceShopId = source.get("shopId");
                if (sourceShopId != null && sourceShopId.isTextual()) {
                    return Result.success(new ShopInfo(app, sourceShopId.textValue()));
                }
            }
            return Result.error("ShopId not found in request body");
        } catch (IOException e) {
            return Result.error("Request body is not valid JSON. %s", e.getMessage());
        }
    }


    private boolean isSignatureValid(byte[] payload, String shopwareShopSignature, ShopInfo shopInfo) {
        if (shopInfo == null) {
            return false;
        }
        return shopManagementService.getShopById(shopInfo.app, shopInfo.shopId)
                .map(ShopwareShopEntity::getShopSecret)
                .map(shopSecret -> signatureService.verifySignature(payload, shopSecret, shopwareShopSignature))
                .orElse(false);
    }

    private Result<ShopInfo> getShopInfoIfGetRequestValid(HttpServletRequest request, ShopwareApp app) {
        String shopId = request.getParameter("shop-id");
        if (shopId == null) {
            return Result.error("Missing shop-id parameter");
        }
        String signature = request.getParameter(ApiConstants.SHOPWARE_SHOP_SIGNATURE_HEADER);
        if (signature == null) {
            return Result.error("Missing signature parameter");
        }
        var info = new ShopInfo(app, shopId);
        // Shopware should never add the signature as the first parameter of the query string.
        // At least the official PHP-SDK expects the signature parameter to be concatenated to the query string with a &
        String queryWithoutSignature = request.getQueryString().replace(String.format("&%s=%s", ApiConstants.SHOPWARE_SHOP_SIGNATURE_HEADER, signature), "");
        if (isSignatureValid(queryWithoutSignature.getBytes(StandardCharsets.UTF_8), signature, info)) {
            return Result.success(info);
        }
        return Result.error("Invalid signature");
    }
}
