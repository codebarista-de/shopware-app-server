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
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Check the {@code shopware-shop-signature} header.
 * <p>
 * This filter cannot be a bean, we only want to apply it to specific App-Server URLs.
 * A GenericFilter bean in Spring Boot is automatically used for any request.
 * This is undesired as it must not apply to all URLs exposed by App implementations.
 */
public class ShopwareSignatureVerificationFilter extends OncePerRequestFilter {
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
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        if (isStaticResource(request.getRequestURI())) {
            filterChain.doFilter(request, response); // Continue without processing
            return;
        }

        final String host = request.getHeader(HttpHeaders.HOST);
        final ShopwareApp app = appLookupService.tryGetForHost(host);
        if (app == null) {
            LOGGER.atWarn()
                .setMessage("Shop authentication failed: No app for host {}.")
                .addArgument(host)
                .addKeyValue("request", request)
                .log();
        } else if (HttpMethod.GET.matches(request.getMethod())) {
            String signature = request.getParameter(ApiConstants.SHOPWARE_SHOP_SIGNATURE_HEADER);
            byte[] message = getValidationQueryString(request, signature);
            String shopId = request.getParameter("shop-id");
            checkSignatureAndSetAuthentication(request, app, shopId, signature, message);
            filterChain.doFilter(request, response);
            return;
        } else if (HttpMethod.POST.matches(request.getMethod())) {
            // To validate a post request the complete request body must be read.
            // Wrap the request so that the body can be read multiple times.
            var reusableRequest = new ReusableRequestWrapper(request);
            String signature = reusableRequest.getHeader(ApiConstants.SHOPWARE_SHOP_SIGNATURE_HEADER);
            byte[] body = reusableRequest.getInputStream().readAllBytes();
            String shopId = getShopIdFromBody(body);
            checkSignatureAndSetAuthentication(reusableRequest, app, shopId, signature, body);
            // Pass the wrapped request to the filter chain!
            filterChain.doFilter(reusableRequest, response);
            return;
        } else {
            LOGGER.atWarn()
                .setMessage("Shop authentication failed: Unsupported method {}.")
                .addArgument(request.getMethod())
                .addKeyValue("app", app.getAppName())
                .addKeyValue("request", request)
                .log();
        }

        filterChain.doFilter(request, response);
    }

    private boolean isStaticResource(String uri) {
        // static resource file endings
        return uri.matches(".*\\.(js|css|ttf|woff|woff2|eot|svg|jpg|jpeg|png|gif|ico)$");
    }

    private String getShopIdFromBody(byte[] body) throws IOException {
        try {
            JsonNode node = objectMapper.readTree(body);
            if (!node.isObject()) {
                LOGGER.warn("Request body is not a JSON object");
                return null;
            }
            // The registration confirmation request has the shopId property directly in the root object
            JsonNode rootShopId = node.get("shopId");
            if (rootShopId != null && rootShopId.isTextual()) {
                return rootShopId.textValue();
            }
            // All other requests have the shopId in the source object
            JsonNode source = node.get("source");
            if (source != null && source.isObject()) {
                JsonNode sourceShopId = source.get("shopId");
                if (sourceShopId != null && sourceShopId.isTextual()) {
                    return sourceShopId.textValue();
                }
            }
            LOGGER.warn("ShopId not found in request body");
        } catch (IOException e) {
            LOGGER.warn("Malformed JSON body.", e);
        }
        return null;
    }

    private byte[] getValidationQueryString(HttpServletRequest request, String signature) {
        String queryString = request.getQueryString();
        if (signature == null || queryString == null) {
            return new byte[0];
        }
        // Shopware should never add the signature as the first parameter of the query string.
        // At least the official PHP-SDK expects the signature parameter to be concatenated to the query string with a &
        String queryWithoutSignature = queryString.replace(String.format("&%s=%s", ApiConstants.SHOPWARE_SHOP_SIGNATURE_HEADER, signature), "");
        return queryWithoutSignature.getBytes(StandardCharsets.UTF_8);
    }

    private void checkSignatureAndSetAuthentication(HttpServletRequest request, ShopwareApp app, String shopId, String signature, byte[] message) {
        if (shopId == null || shopId.isBlank()) {
            LOGGER.atWarn()
                .setMessage("Shop authentication failed: Missing shop ID.")
                .addKeyValue("app", app.getAppName())
                .addKeyValue("request", request)
                .log();
            return;
        }
        if (signature == null || signature.isBlank()) {
            LOGGER.atWarn()
                .setMessage("Shop authentication failed: Missing signature.")
                .addKeyValue("app", app.getAppName())
                .addKeyValue("shop-id", shopId)
                .addKeyValue("request", request)
                .log();
            return;
        }
        ShopwareShopEntity shop = shopManagementService.getShopById(app, shopId).orElse(null);
        if (shop == null) {
            LOGGER.atWarn()
                .setMessage("Shop authentication failed: Unknown shop ID.")
                .addKeyValue("app", app.getAppName())
                .addKeyValue("shop-id", shopId)
                .addKeyValue("request", request)
                .log();
            return;
        }
        if (signatureService.verifySignature(message, shop.getShopSecret(), signature)) {
            setAuthentication(shopId, AppServerWebSecurityConfiguration.ROLE_SHOPWARE_SHOP);
        } else if (signatureService.verifySignature(message, shop.getPendingShopSecret(), signature)) {
            // If a shop is registered for the first time or if an existing shop is re-registered, the
            // shop secret is pending until it has been confirmed. The confirm endpoint is protected with
            // the pending signature. Since it's a POST endpoint, signature verification needs the raw
            // request body. By the time the controller method runs, Spring has already consumed the
            // InputStream to populate @RequestBody, so the verification must happen here in the filter
            // using the ReusableRequestWrapper.
            setAuthentication(shopId, AppServerWebSecurityConfiguration.ROLE_SHOPWARE_PENDING_SHOP);
        } else {
            LOGGER.atWarn()
                .setMessage("Shop authentication failed: Invalid signature.")
                .addKeyValue("app", app.getAppName())
                .addKeyValue("shop-id", shopId)
                .addKeyValue("request", request)
                .log();
        }
    }

    private void setAuthentication(String principal, String authority) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority(authority)))
        );
    }
}
