package de.codebarista.shopware.appserver.config;

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
 * Central security configuration for the Shopware SDK.
 * <p>
 * This class defines the security rules for all SDK-specific endpoints under:
 * <ul>
 *   <li>{@code /shopware/api/v1/**} – API endpoints for Shopware integration.</li>
 *   <li>{@code /shopware/admin/**} – Admin extension endpoints (e.g., for serving assets).</li>
 * </ul>
 * <p>
 * The security configuration is applied with high priority ({@link Order @Order(1)})
 * to ensure SDK rules are evaluated before any application-specific security rules.
 * <p>
 * <h2>Security Features</h2>
 * <ul>
 *   <li>
 *     <strong>Stateless Sessions:</strong>
 *     All SDK endpoints are stateless (no session cookies).
 *   </li>
 *   <li>
 *     <strong>CSRF Protection Disabled:</strong>
 *     CSRF protection is disabled for SDK endpoints (assumes API clients use tokens/signatures).
 *   </li>
 *   <li>
 *     <strong>Signature Verification:</strong>
 *     All requests to SDK endpoints are verified using the {@link ShopwareSignatureVerificationFilter}.
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
 * The SDK provides default implementations for:
 * <ul>
 *   <li>
 *     {@link PasswordEncoder}: A {@link BCryptPasswordEncoder} is provided by default.
 *     Override by defining your own {@link PasswordEncoder} bean in your application context.
 *   </li>
 * </ul>
 * <p>
 * <strong>Note:</strong>
 * This configuration <em>only</em> applies to SDK endpoints ({@code /shopware/**}).
 * Application endpoints must be secured separately in the host application.
 * <p>
 * <h2>Example: Overriding Default Beans</h2>
 * <pre>{@code
 * @Configuration
 * public class AppSecurityConfig {
 *
 *     // Override the default PasswordEncoder
 *     @Bean
 *     public PasswordEncoder passwordEncoder() {
 *         return new Argon2PasswordEncoder();
 *     }
 * }
 * }</pre>
 * <p>
 * <h2>Endpoint Authorization Rules</h2>
 * <table border="1">
 *   <caption>Default Authorization Rules</caption>
 *   <tr>
 *     <th>Endpoint</th>
 *     <th>HTTP Method</th>
 *     <th>Required Authority</th>
 *   </tr>
 *   <tr>
 *     <td>{@code /shopware/api/v1/registration/register}</td>
 *     <td>GET</td>
 *     <td>{@link #ROLE_SHOPWARE_APP}</td>
 *   </tr>
 *   <tr>
 *     <td>{@code /shopware/api/v1/<em>path</em>}</td>
 *     <td>ALL</td>
 *     <td>{@link #ROLE_SHOPWARE_SHOP}</td>
 *   </tr>
 *   <tr>
 *     <td>{@code /shopware/admin/<em>path1</em>/<em>path2</em>/assets/<em>path</em>}</td>
 *     <td>GET</td>
 *     <td>None (public)</td>
 *   </tr>
 *   <tr>
 *     <td>{@code /shopware/admin/<em>path</em>}</td>
 *     <td>ALL</td>
 *     <td>{@link #ROLE_SHOPWARE_SHOP}</td>
 *   </tr>
 * </table>
 */
@Configuration
@Order(1) // high priority, evaluate first
public class AppServerWebSecurityConfiguration {

    public static final String ROLE_SHOPWARE_SHOP = "ROLE_SHOPWARE_SHOP";
    public static final String ROLE_SHOPWARE_APP = "ROLE_SHOPWARE_APP";

    private final ShopwareSignatureVerificationFilter signatureVerificationFilter;

    public AppServerWebSecurityConfiguration(ShopwareSignatureVerificationFilter signatureVerificationFilter) {
        this.signatureVerificationFilter = signatureVerificationFilter;
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