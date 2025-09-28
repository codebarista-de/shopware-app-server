package de.codebarista.shopware.appserver.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.codebarista.shopware.appserver.service.AppLookupService;
import de.codebarista.shopware.appserver.service.ShopManagementService;
import de.codebarista.shopware.appserver.service.SignatureService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;


/**
 * Central security configuration for the Shopware App Server.
 * <p>
 * This class defines the security rules for all App Server-specific endpoints under:
 * <ul>
 *   <li>{@code /shopware/api/v1/**} – API endpoints for Shopware integration.</li>
 *   <li>{@code /shopware/admin/**} – Admin extension endpoints (e.g., for serving assets).</li>
 * </ul>
 * <p>
 * <p>
 * <h2>Security Features</h2>
 * <ul>
 *   <li>
 *     <strong>Stateless Sessions:</strong>
 *     All App Server endpoints are stateless (no session cookies).
 *   </li>
 *   <li>
 *     <strong>CSRF Protection Disabled:</strong>
 *     CSRF protection is disabled for App Server endpoints (assumes API clients use tokens/signatures).
 *   </li>
 *   <li>
 *     <strong>Signature Verification:</strong>
 *     All requests to App Server endpoints are verified using the {@link ShopwareSignatureVerificationFilter}.
 *     This filter is <em>mandatory</em> and cannot be disabled in the current version.
 *   </li>
 *   <li>
 *     <strong>Role-Based Access Control:</strong>
 *     Endpoints require either {@link #ROLE_SHOPWARE_SHOP} or {@link #ROLE_SHOPWARE_APP}.
 *     See individual endpoint rules for details.
 *   </li>
 * </ul>
 * <p>
 * <h2>Customization</h2>
 * <p>
 * The App Server provides default implementations for:
 * <ul>
 *   <li>
 *     {@link PasswordEncoder}: A {@link BCryptPasswordEncoder} is provided by default.
 *     Override by defining your own {@link PasswordEncoder} bean in your application context.
 *   </li>
 * </ul>
 * <p>
 * <strong>Note:</strong>
 * This configuration <em>only</em> applies to App Server endpoints ({@code /shopware/**}).
 * Application endpoints must be secured separately in the host application.
 * See {@link de.codebarista.shopware.appserver.TokenService}
 */
@Configuration
public class AppServerWebSecurityConfiguration {

    public static final String ROLE_SHOPWARE_SHOP = "ROLE_SHOPWARE_SHOP";
    public static final String ROLE_SHOPWARE_APP = "ROLE_SHOPWARE_APP";

    private final ShopwareSignatureVerificationFilter signatureVerificationFilter;

    public AppServerWebSecurityConfiguration(
            ShopManagementService shopManagementService,
            SignatureService signatureService,
            AppLookupService appLookupService,
            ObjectMapper objectMapper
    ) {
        this.signatureVerificationFilter = new ShopwareSignatureVerificationFilter(shopManagementService,
                signatureService, appLookupService, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean(PasswordEncoder.class)
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain shopwareApiSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/shopware/api/v1/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterAfter(signatureVerificationFilter, BasicAuthenticationFilter.class)
                .authorizeHttpRequests(ar -> ar
                        .requestMatchers(HttpMethod.GET, "/shopware/api/v1/registration/register").hasAuthority(ROLE_SHOPWARE_APP)
                        .anyRequest().hasAuthority(ROLE_SHOPWARE_SHOP))
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .build();
    }

    @Bean
    public SecurityFilterChain adminExtensionsSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/shopware/admin/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterAfter(signatureVerificationFilter, BasicAuthenticationFilter.class)
                .authorizeHttpRequests(ar -> ar
                        .requestMatchers(HttpMethod.GET, "/shopware/admin/*/*/assets/**").permitAll()
                        .anyRequest().hasAuthority(ROLE_SHOPWARE_SHOP))
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .build();
    }
}