package de.codebarista.shopware.appserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.codebarista.shopware.appserver.api.dto.action.ActionResponseDto;
import de.codebarista.shopware.appserver.api.dto.registration.ShopwareAppConfirmationDto;
import de.codebarista.shopware.appserver.api.dto.registration.ShopwareAppRegistrationResponseDto;
import de.codebarista.shopware.appserver.model.ShopwareShopEntityRepository;
import de.codebarista.shopware.testutils.TestAppA;
import de.codebarista.shopware.testutils.TestAppB;
import de.codebarista.shopware.testutils.TestHelper;
import de.codebarista.shopware.testutils.WebServerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;

import java.util.Objects;

import static de.codebarista.shopware.appserver.api.ApiConstants.SHOPWARE_APP_SIGNATURE_HEADER;
import static org.assertj.core.api.Assertions.assertThat;

@WebServerTest
public class AppRegistrationApiTest {
    private static final String URL_REGISTER = "/shopware/api/v1/registration/register";
    private static final String URL_CONFIRM = "/shopware/api/v1/registration/confirm";
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private ShopwareShopEntityRepository shopwareShopEntityRepository;

    public static ShopwareAppRegistrationResponseDto registerShopForApp(WebTestClient webTestClient, ShopwareApp app, String shopId, String shopUrl) {
        return requestRegisterShopForApp(webTestClient, app, shopId, shopUrl)
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(ShopwareAppRegistrationResponseDto.class)
                .returnResult()
                .getResponseBody();
    }

    public static WebTestClient.ResponseSpec requestRegisterShopForApp(WebTestClient webTestClient, ShopwareApp app, String shopId, String shopUrl) {
        final UriComponents registrationUriComponents = UriComponentsBuilder.fromPath(URL_REGISTER)
                .queryParam("shop-id", shopId)
                .queryParam("shop-url", shopUrl)
                .queryParam("timestamp", "1701363018")
                .build();

        String query = Objects.requireNonNull(registrationUriComponents.getQuery());
        String shopwareAppSignature = TestHelper.hmac256(query, app.getAppSecret());
        String registrationUrl = registrationUriComponents.toString();

        return webTestClient.get()
                .uri(registrationUrl)
                .header(HttpHeaders.HOST, app.getAppKey() + ".app-backend.de")
                .header(SHOPWARE_APP_SIGNATURE_HEADER, shopwareAppSignature)
                .exchange();
    }

    public static ShopwareAppRegistrationResponseDto registerShopForAppWithShopSignature(WebTestClient webTestClient, ShopwareApp app, String shopId, String shopUrl, String shopSecret) {
        return requestRegisterShopForAppWithShopSignature(webTestClient, app, shopId, shopUrl, shopSecret)
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(ShopwareAppRegistrationResponseDto.class)
                .returnResult()
                .getResponseBody();
    }

    public static WebTestClient.ResponseSpec requestRegisterShopForAppWithShopSignature(
            WebTestClient webTestClient, ShopwareApp app, String shopId, String shopUrl, String shopSecret) {
        final UriComponents registrationUriComponents = UriComponentsBuilder.fromPath(URL_REGISTER)
                .queryParam("shop-id", shopId)
                .queryParam("shop-url", shopUrl)
                .queryParam("timestamp", "1701363018")
                .build();

        String query = Objects.requireNonNull(registrationUriComponents.getQuery());
        String shopwareAppSignature = TestHelper.hmac256(query, app.getAppSecret());
        String shopwareShopSignature = TestHelper.hmac256(query, shopSecret);
        String registrationUrl = registrationUriComponents.toString();

        return webTestClient.get()
                .uri(registrationUrl)
                .header(HttpHeaders.HOST, app.getAppKey() + ".app-backend.de")
                .header(SHOPWARE_APP_SIGNATURE_HEADER, shopwareAppSignature)
                .header("shopware-shop-signature", shopwareShopSignature)
                .exchange();
    }

    public static WebTestClient.ResponseSpec requestConfirmRegistration(WebTestClient webTestClient, ShopwareApp app, String shopSecret, String shopId, String shopUrl) {
        try {
            byte[] body = new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsBytes(
                    new ShopwareAppConfirmationDto(
                            "apiKey",
                            "secretKey",
                            "1701363030",
                            shopUrl,
                            shopId
                    ));
            String signature = TestHelper.hmac256(body, shopSecret);
            return webTestClient.post()
                    .uri(URL_CONFIRM)
                    .header(HttpHeaders.HOST, app.getAppKey() + ".app-backend.de")
                    .header("shopware-shop-signature", signature)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .exchange();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static WebTestClient.ResponseSpec requestLifecycleDeleted(
            WebTestClient webTestClient, ShopwareApp app, String shopSecret, String shopId, String shopUrl) {
        String body = String.format(
                "{\"timestamp\":\"1702212669\",\"data\":{\"event\":\"app.deleted\",\"payload\":[]},\"source\":{\"appVersion\":\"0.0.1\",\"shopId\":\"%s\",\"eventId\":\"e4ent1d\",\"url\":\"%s\"}}",
                shopId, shopUrl);
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        String signature = TestHelper.hmac256(bodyBytes, shopSecret);

        return webTestClient.post()
                .uri("/shopware/api/v1/lifecycle/deleted")
                .header(HttpHeaders.HOST, app.getAppKey() + ".app-backend.de")
                .header("shopware-shop-signature", signature)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(bodyBytes)
                .exchange();
    }

    public static WebTestClient.ResponseSpec sendAction(
            WebTestClient webTestClient, ShopwareApp app, String shopSecret, String shopId, String shopUrl, String action) {
        String body = String.format(
                "{\"timestamp\":\"1702212669\",\"data\":{\"action\":\"" + action + "\"},\"source\":{\"appVersion\":\"0.0.1\",\"shopId\":\"%s\",\"eventId\":\"e4ent1d\",\"url\":\"%s\"},\"meta\":{}}",
                shopId, shopUrl);
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        String signature = TestHelper.hmac256(bodyBytes, shopSecret);

        return webTestClient.post()
                .uri("/shopware/api/v1/action")
                .header(HttpHeaders.HOST, app.getAppKey() + ".app-backend.de")
                .header("shopware-shop-signature", signature)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(bodyBytes)
                .exchange();
    }

    @Test
    public void registerAndConfirmShop() {
        final String shopID = "jmpF3ttj2rEnvmfZ";
        final String shopUrl = "https://my-shop.de";

        var app = new TestAppA();
        var registrationResponse = registerShopForApp(webTestClient, app, shopID, shopUrl);
        assertThat(registrationResponse.getConfirmationUrl())
                .endsWith(URL_CONFIRM);
        assertThat(registrationResponse.getShopSecret()).isNotNull();
        String expectedProof = TestHelper.hmac256(shopID + shopUrl + app.getAppName(), app.getAppSecret());
        assertThat(registrationResponse.getProof()).isEqualTo(expectedProof);
        assertThat(registrationResponse.getError()).isNull();

        // Send confirmation request
        requestConfirmRegistration(webTestClient, app, registrationResponse.getShopSecret(), shopID, shopUrl).expectStatus().is2xxSuccessful();
        
        // Check that shop is confirmed by sending an action
        ActionResponseDto<?> response = sendAction(webTestClient, app, registrationResponse.getShopSecret(), shopID, shopUrl, "foo")
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(ActionResponseDto.class)
                .returnResult()
                .getResponseBody();
        assertThat(response).extracting("payload.message").isEqualTo("foo");
    }

    @Test
    public void reRegisterShop() {
        final String shopID = "jmpF3ttj2rEnvmfZ";
        final String shopUrl = "https://myshop.de";

        var app = new TestAppA();
        var registrationResponse = registerShopForApp(webTestClient, app, shopID, shopUrl);
        requestConfirmRegistration(webTestClient, app, registrationResponse.getShopSecret(), shopID, shopUrl).expectStatus().is2xxSuccessful();

        // Re-Register shop
        var reregistrationResponse = registerShopForApp(webTestClient, app, shopID, shopUrl);
        requestConfirmRegistration(webTestClient, app, reregistrationResponse.getShopSecret(), shopID, shopUrl).expectStatus().is2xxSuccessful();

        ActionResponseDto<?> response = sendAction(webTestClient, app, reregistrationResponse.getShopSecret(), shopID, shopUrl, "reregister-test")
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(ActionResponseDto.class)
                .returnResult()
                .getResponseBody();
        assertThat(response).extracting("payload.message").isEqualTo("reregister-test");
    }

    @Test
    public void reRegisterShopWithDifferentUrl() {
        final String shopID = "jmpF3ttj2rEnvmfZ";
        final String oldShopUrl = "https://myshop-old.de";
        final String newShopUrl = "https://myshop-new.de";

        var app = new TestAppA();
        var registrationResponse = registerShopForApp(webTestClient, app, shopID, oldShopUrl);
        requestConfirmRegistration(webTestClient, app, registrationResponse.getShopSecret(), shopID, oldShopUrl).expectStatus().is2xxSuccessful();

        // Re-Register shop
        var reregistrationResponse = registerShopForApp(webTestClient, app, shopID, newShopUrl);
        requestConfirmRegistration(webTestClient, app, reregistrationResponse.getShopSecret(), shopID, newShopUrl).expectStatus().is2xxSuccessful();


        ActionResponseDto<?> response = sendAction(webTestClient, app, reregistrationResponse.getShopSecret(), shopID, newShopUrl, "reregister-new-url-test")
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(ActionResponseDto.class)
                .returnResult()
                .getResponseBody();
        assertThat(response).extracting("payload.message").isEqualTo("reregister-new-url-test");
    }

    @Test
    public void registrationFailsWithInvalidAppSubDomain() {
        final String query = "shop-id=jmpF3ttj2rEnvmfZ&shop-url=http://127.0.0.1:8000&timestamp=1701363018";
        final String shopwareAppSignature = TestHelper.hmac256(query, new TestAppA().getAppSecret());
        webTestClient.get()
                .uri(URL_REGISTER + "?" + query)
                .header(HttpHeaders.HOST, "devil.app-backend.de")
                .header(SHOPWARE_APP_SIGNATURE_HEADER, shopwareAppSignature)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    public void registrationFailsWithEmptyShopID() {
        requestRegisterShopForApp(webTestClient, new TestAppA(), "", "https://myshop.de")
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    public void registrationFailsWithEmptyShopURL() {
        requestRegisterShopForApp(webTestClient, new TestAppA(), "1234", "")
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    public void registrationFailsWithInvalidAppSignature() {
        final var app = new TestAppA();
        String query = "shop-id=jmpF3ttj2rEnvmfZ&shop-url=http://127.0.0.1:8000&timestamp=1701363018";
        String registrationUri = URL_REGISTER + "?" + query;
        String validSignature = TestHelper.hmac256(query, app.getAppSecret());
        String invalidSignature = "ff" + validSignature.substring(2);

        // Check that registration works with the correct signature and if it's repeated multiple times.
        // This ensures that the request with the invalid signature fails because of the signature and not
        // for some other reason.
        for (int i = 0; i < 2; i++) {
            webTestClient.get()
                    .uri(registrationUri)
                    .header(HttpHeaders.HOST, app.getAppKey() + ".app-backend.de")
                    .header(SHOPWARE_APP_SIGNATURE_HEADER, validSignature)
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful();
        }

        webTestClient.get()
                .uri(registrationUri)
                .header(HttpHeaders.HOST, app.getAppKey() + ".app-backend.de")
                .header(SHOPWARE_APP_SIGNATURE_HEADER, invalidSignature)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    public void confirmationFailsWithInvalidSignature() {
        final var app = new TestAppA();
        final String shopUrl = "https://myshop.de";
        final String shopId = "shop-id";

        registerShopForApp(webTestClient, app, shopId, shopUrl);
        requestConfirmRegistration(webTestClient, app, "wrong-shop-secret", shopId, shopUrl).expectStatus().isUnauthorized();
    }

    @Test
    public void confirmationFailsWithWrongShopUrl() {
        final var app = new TestAppA();
        final String shopUrl = "https://myshop.de";
        final String shopId = "shop-id";

        var registrationResponse =  registerShopForApp(webTestClient, app, shopId, shopUrl);
        requestConfirmRegistration(webTestClient, app, registrationResponse.getShopSecret(), shopId, shopUrl + ".test").expectStatus().isUnauthorized();
    }

    @Test
    public void registerSameShopForDifferentApps() {
        final var appA = new TestAppA();
        final var appB = new TestAppB();

        final String shopUrl = "https://myshop.de";
        final String shopId = "shop-id";

        final var registerAResponse = registerShopForApp(webTestClient, appA, shopId, shopUrl);
        requestConfirmRegistration(webTestClient, appA, registerAResponse.getShopSecret(), shopId, shopUrl).expectStatus().is2xxSuccessful();

        final var registerBResponse = registerShopForApp(webTestClient, appB, shopId, shopUrl);
        requestConfirmRegistration(webTestClient, appB, registerBResponse.getShopSecret(), shopId, shopUrl).expectStatus().is2xxSuccessful();
    }

    @Test
    public void differentShopSecretsAreGeneratedForDifferentShops() {
        final var app = new TestAppA();

        final String firstShopUrl = "https://second-shop.de";
        final String firstShopId = "first-shop-id";

        final String secondShopUrl = "https://second-shop.de";
        final String secondShopId = "first-shop-id";

        final var registerFirstResponse = registerShopForApp(webTestClient, app, firstShopId, firstShopUrl);
        final var registerSecondResponse = registerShopForApp(webTestClient, app, secondShopId, secondShopUrl);

        assertThat(registerFirstResponse.getShopSecret()).isNotEqualTo(registerSecondResponse.getShopSecret());
    }

    @Test
    public void registeringAShopMultipleTimesDoesNotGenerateMultipleDatabaseEntries() {
        final var app = new TestAppA();
        final String shopUrl = "https://myshop.de";
        final String shopId = "shop-id";

        for (int i = 0; i < 100; i++) {
            registerShopForApp(webTestClient, app, shopId, shopUrl);
        }

        assertThat(shopwareShopEntityRepository.count()).isEqualTo(1);
    }

    @Test
    public void reRegisterConfirmedShopKeepsOldSecretActive() {
        var app = new TestAppA();
        String shopId = "test-shop-rotation-1";
        String shopUrl = "https://myshop.de";

        // Register and confirm
        var registration = registerShopForApp(webTestClient, app, shopId, shopUrl);
        String oldSecret = registration.getShopSecret();
        requestConfirmRegistration(webTestClient, app, oldSecret, shopId, shopUrl)
                .expectStatus().is2xxSuccessful();

        // Re-register (get new secret)
        var reRegistration = registerShopForApp(webTestClient, app, shopId, shopUrl);
        String newSecret = reRegistration.getShopSecret();
        assertThat(newSecret).isNotEqualTo(oldSecret);

        // Lifecycle request with OLD secret should succeed (old secret still active)
        requestLifecycleDeleted(webTestClient, app, oldSecret, shopId, shopUrl)
                .expectStatus().is2xxSuccessful();
    }

    @Test
    public void reRegisterConfirmedShopNewSecretNotYetActive() {
        var app = new TestAppA();
        String shopId = "test-shop-rotation-2";
        String shopUrl = "https://myshop.de";

        // Register and confirm
        var registration = registerShopForApp(webTestClient, app, shopId, shopUrl);
        String oldSecret = registration.getShopSecret();
        requestConfirmRegistration(webTestClient, app, oldSecret, shopId, shopUrl)
                .expectStatus().is2xxSuccessful();

        // Re-register (get new secret)
        var reRegistration = registerShopForApp(webTestClient, app, shopId, shopUrl);
        String newSecret = reRegistration.getShopSecret();

        // Lifecycle request with NEW secret should fail (pending, not active yet)
        requestLifecycleDeleted(webTestClient, app, newSecret, shopId, shopUrl)
                .expectStatus().isUnauthorized();
    }

    @Test
    public void unRegisteredCannotBeUsedForAuthentication() {
        final String shopID = "jmpF3ttj2rEnvmfZ";
        final String shopUrl = "https://my-shop.de";

        var app = new TestAppA();

        // Action request should fail (shop is not registered)
        sendAction(webTestClient, app, "foobar", shopID, shopUrl, "foo").expectStatus().isUnauthorized();
        
    }

    @Test
    public void unconfirmedShopSecretCannotBeUsedForAuthentication() {
        var app = new TestAppA();
        String shopId = "test-shop-unconfirmed";
        String shopUrl = "https://myshop.de";

        // Register (no confirm)
        var registration = registerShopForApp(webTestClient, app, shopId, shopUrl);
        String shopSecret = registration.getShopSecret();

        // Action request should fail (shop is not confirmed)
        sendAction(webTestClient, app, shopSecret, shopId, shopUrl, "foo").expectStatus().isUnauthorized();
    }

    @Test
    public void shopSecretCannotBeUsedForConfirmationOfOtherShop() {
        var app = new TestAppA();
        String shopIdA = "shop-a";
        String shopUrlA = "https://a-shop.de";
        String shopIdB = "shop-b";
        String shopUrlB = "https://b-shop.de";

        // Register but do not confirm both shops
        var registrationA = registerShopForApp(webTestClient, app, shopIdA, shopUrlA);
        var registrationB = registerShopForApp(webTestClient, app, shopIdB, shopUrlB);

        // Try to confirm shop B with secret of shop A and vice-versa -> should fail
        requestConfirmRegistration(webTestClient, app, registrationA.getShopSecret(), shopIdB, shopUrlB).expectStatus().isUnauthorized();
        requestConfirmRegistration(webTestClient, app, registrationB.getShopSecret(), shopIdA, shopUrlA).expectStatus().isUnauthorized();

        // Sending events should fails as shops are unconfirmed
        sendAction(webTestClient, app, registrationA.getShopSecret(), shopIdB, shopUrlB, "foo-b").expectStatus().isUnauthorized();
        sendAction(webTestClient, app, registrationB.getShopSecret(), shopIdA, shopUrlA, "foo-a").expectStatus().isUnauthorized();
    }

    @Test
    public void shopSecretCannotBeUsedForAuthenticationOfOtherShop() {
        var app = new TestAppA();
        String shopIdA = "shop-a";
        String shopUrlA = "https://a-shop.de";
        String shopIdB = "shop-b";
        String shopUrlB = "https://b-shop.de";

        // Register and confirm both shops
        var registrationA = registerShopForApp(webTestClient, app, shopIdA, shopUrlA);
        requestConfirmRegistration(webTestClient, app, registrationA.getShopSecret(), shopIdA, shopUrlA).expectStatus().is2xxSuccessful();
        var registrationB = registerShopForApp(webTestClient, app, shopIdB, shopUrlB);
        requestConfirmRegistration(webTestClient, app, registrationB.getShopSecret(), shopIdB, shopUrlB).expectStatus().is2xxSuccessful();

        // Sending event to shop B with secret of shop A and vice-versa should fail
        sendAction(webTestClient, app, registrationA.getShopSecret(), shopIdB, shopUrlB, "foo-b").expectStatus().isUnauthorized();
        sendAction(webTestClient, app, registrationB.getShopSecret(), shopIdA, shopUrlA, "foo-a").expectStatus().isUnauthorized();
    }

    @Test
    public void reRegisterUnconfirmedShopNewSecretNotImmediatelyActive() {
        var app = new TestAppA();
        String shopId = "test-shop-unconfirmed-rereg";
        String shopUrl = "https://myshop.de";

        // Register (no confirm)
        registerShopForApp(webTestClient, app, shopId, shopUrl);

        // Re-register (get new secret)
        var reRegistration = registerShopForApp(webTestClient, app, shopId, shopUrl);
        String newSecret = reRegistration.getShopSecret();

        // Lifecycle request with new secret should fail
        requestLifecycleDeleted(webTestClient, app, newSecret, shopId, shopUrl)
                .expectStatus().isUnauthorized();
    }

    @Test
    public void reRegistrationOfUnconfirmedShopOverwritesPreviousRegistration() {
        var app = new TestAppA();
        String shopId = "test-shop-rereg-requires-shop-sig-1";
        String shopUrl = "https://myshop.de";

        // Register but do not confirm
        var registration1 = registerShopForApp(webTestClient, app, shopId, shopUrl);

        // Re-register
        var registration2 = registerShopForApp(webTestClient, app, shopId, shopUrl);

        // Try to confirm with secret from first registration -> should fail
        requestConfirmRegistration(webTestClient, app, registration1.getShopSecret(), shopId, shopUrl)
                .expectStatus().isUnauthorized();
                
        // Confirmation with secret for last registration should work
        requestConfirmRegistration(webTestClient, app, registration2.getShopSecret(), shopId, shopUrl)
                .expectStatus().is2xxSuccessful();
    }

    @Test
    public void reRegistrationOfConfirmedShopOverwritesPreviousReRegistration() {
        var app = new TestAppA();
        String shopId = "test-shop-rereg-requires-shop-sig-1";
        String shopUrl = "https://myshop.de";

        // Register and confirm
        var registration = registerShopForApp(webTestClient, app, shopId, shopUrl);
        requestConfirmRegistration(webTestClient, app, registration.getShopSecret(), shopId, shopUrl)
                .expectStatus().is2xxSuccessful();

        // Re-register
        var reregistration1 = registerShopForApp(webTestClient, app, shopId, shopUrl);

        // Re-register again
        var reregistration2 = registerShopForApp(webTestClient, app, shopId, shopUrl);

        // Try to confirm with secret from first re-registration -> should fail
        requestConfirmRegistration(webTestClient, app, reregistration1.getShopSecret(), shopId, shopUrl)
                .expectStatus().isUnauthorized();
                
        // Confirmation with secret for last re-registration should work
        requestConfirmRegistration(webTestClient, app, reregistration2.getShopSecret(), shopId, shopUrl)
                .expectStatus().is2xxSuccessful();
    }

    @Test
    public void reRegistrationRequiresShopSignatureAfterPreviouslyVerified() {
        var app = new TestAppA();
        String shopId = "test-shop-rereg-requires-shop-sig-1";
        String shopUrl = "https://myshop.de";

        // Register and confirm
        var registration = registerShopForApp(webTestClient, app, shopId, shopUrl);
        String shopSecret = registration.getShopSecret();
        requestConfirmRegistration(webTestClient, app, shopSecret, shopId, shopUrl)
                .expectStatus().is2xxSuccessful();

        // Re-register with both app + valid shop signature
        // From now on the shop signature shall always be required for re-registration of this shop
        requestRegisterShopForAppWithShopSignature(webTestClient, app, shopId, shopUrl, shopSecret)
                .expectStatus().is2xxSuccessful();

        // Re-register again with only app signature -> expect failure
        requestRegisterShopForApp(webTestClient, app, shopId, shopUrl)
                .expectStatus().isUnauthorized();
    }

    @Test
    public void reRegistrationRequiresShopSignatureOnlyForPreviouslyVerifiedShop() {
        var app = new TestAppA();
        String shopIdA = "test-shop-a";
        String shopUrlA = "https://myshop-a.de";
        String shopIdB = "test-shop-b";
        String shopUrlB = "https://myshop-b.de";

        // Register and confirm both shops
        var registrationA = registerShopForApp(webTestClient, app, shopIdA, shopUrlA);
        String shopSecretA = registrationA.getShopSecret();
        requestConfirmRegistration(webTestClient, app, shopSecretA, shopIdA, shopUrlA)
                .expectStatus().is2xxSuccessful();
        var registrationB = registerShopForApp(webTestClient, app, shopIdB, shopUrlB);
        String shopSecretB = registrationB.getShopSecret();
        requestConfirmRegistration(webTestClient, app, shopSecretB, shopIdB, shopUrlB)
                .expectStatus().is2xxSuccessful();

        // Re-register A only with app signature
        requestRegisterShopForApp(webTestClient, app, shopIdA, shopUrlA)
                .expectStatus().is2xxSuccessful();
        // Re-register B with both app + valid shop signature
        requestRegisterShopForAppWithShopSignature(webTestClient, app, shopIdB, shopUrlB, shopSecretB)
                .expectStatus().is2xxSuccessful();

        // Re-register A again with only app signature -> expect success
        requestRegisterShopForApp(webTestClient, app, shopIdA, shopUrlA)
                .expectStatus().is2xxSuccessful();
        // Re-register B again with only app signature -> expect failure
        requestRegisterShopForApp(webTestClient, app, shopIdB, shopUrlB)
                .expectStatus().isUnauthorized();
    }

    @Test
    public void reRegistrationFailsWithInvalidShopSignatureAfterPreviouslyVerified() {
        var app = new TestAppA();
        String shopId = "test-shop-rereg-requires-shop-sig-2";
        String shopUrl = "https://myshop.de";

        // Register and confirm
        var registration = registerShopForApp(webTestClient, app, shopId, shopUrl);
        String shopSecret = registration.getShopSecret();
        requestConfirmRegistration(webTestClient, app, shopSecret, shopId, shopUrl)
                .expectStatus().is2xxSuccessful();

        // Re-register with both valid signatures
        requestRegisterShopForAppWithShopSignature(webTestClient, app, shopId, shopUrl, shopSecret)
                .expectStatus().is2xxSuccessful();

        // Re-register with app signature + INVALID shop signature -> expect failure
        requestRegisterShopForAppWithShopSignature(webTestClient, app, shopId, shopUrl, "wrong-shop-secret")
                .expectStatus().isUnauthorized();
    }

    @Test
    public void registrationWithShopSignatureIgnoredIfNotConfirmed() {
        var app = new TestAppA();
        String shopId = "test-shop-rereg-requires-shop-sig-1";
        String shopUrl = "https://myshop.de";

        // Register with but do not confirm
        var registration1 = registerShopForApp(webTestClient, app, shopId, shopUrl);

        // Register again with both app + valid shop signature -> should succeed but shop signature is ignored
        var registration2 = registerShopForAppWithShopSignature(webTestClient, app, shopId, shopUrl, registration1.getShopSecret());
        requestConfirmRegistration(webTestClient, app, registration2.getShopSecret(), shopId, shopUrl)
                .expectStatus().is2xxSuccessful();

        // Re-register again with only app signature -> expect success as shop signature was ignored on previous registration
        var reregistration = registerShopForApp(webTestClient, app, shopId, shopUrl);
        requestConfirmRegistration(webTestClient, app, reregistration.getShopSecret(), shopId, shopUrl)
                .expectStatus().is2xxSuccessful();

        requestLifecycleDeleted(webTestClient, app, reregistration.getShopSecret(), shopId, shopUrl)
                .expectStatus().is2xxSuccessful();
    }

    @Test
    public void deletedShopWithShopSignatureRequirementCanBeRegisteredWithoutShopSignature() {
        var app = new TestAppA();
        String shopId = "test-shop-delete-rereg";
        String shopUrl = "https://myshop.de";

        // Register and confirm
        var registration = registerShopForApp(webTestClient, app, shopId, shopUrl);
        String shopSecret = registration.getShopSecret();
        requestConfirmRegistration(webTestClient, app, shopSecret, shopId, shopUrl)
                .expectStatus().is2xxSuccessful();

        // Re-register with shop signature (sets reregistrationRequiresShopSignature=true)
        var reregistration = registerShopForAppWithShopSignature(webTestClient, app, shopId, shopUrl, shopSecret);
        String reregSecret = reregistration.getShopSecret();
        requestConfirmRegistration(webTestClient, app, reregSecret, shopId, shopUrl)
                .expectStatus().is2xxSuccessful();

        // Delete the shop via lifecycle event (hard deletes the row)
        requestLifecycleDeleted(webTestClient, app, reregSecret, shopId, shopUrl)
                .expectStatus().is2xxSuccessful();

        // Register the same shop ID again WITHOUT a shop signature — expect success (the flag is gone with the row)
        var freshRegistration = registerShopForApp(webTestClient, app, shopId, shopUrl);
        String freshSecret = freshRegistration.getShopSecret();
        requestConfirmRegistration(webTestClient, app, freshSecret, shopId, shopUrl)
                .expectStatus().is2xxSuccessful();

        // Verify the shop works
        sendAction(webTestClient, app, freshSecret, shopId, shopUrl, "after-delete-test")
                .expectStatus().is2xxSuccessful();
    }

    @Test
    public void deletedShopCanNoLongerAuthenticate() {
        var app = new TestAppA();
        String shopId = "test-shop-delete-auth";
        String shopUrl = "https://myshop.de";

        // Register and confirm
        var registration = registerShopForApp(webTestClient, app, shopId, shopUrl);
        String shopSecret = registration.getShopSecret();
        requestConfirmRegistration(webTestClient, app, shopSecret, shopId, shopUrl)
                .expectStatus().is2xxSuccessful();

        // Delete the shop via lifecycle event
        requestLifecycleDeleted(webTestClient, app, shopSecret, shopId, shopUrl)
                .expectStatus().is2xxSuccessful();

        // Try to send an action with the old shop secret — expect 401 (shop no longer exists)
        sendAction(webTestClient, app, shopSecret, shopId, shopUrl, "foo")
                .expectStatus().isUnauthorized();
    }

    @Test
    public void registrationFailsWithBlankTimestamp() {
        var app = new TestAppA();
        String query = "shop-id=shop1&shop-url=https://myshop.de&timestamp=";
        String signature = TestHelper.hmac256(query, app.getAppSecret());

        webTestClient.get()
                .uri(URL_REGISTER + "?" + query)
                .header(HttpHeaders.HOST, app.getAppKey() + ".app-backend.de")
                .header(SHOPWARE_APP_SIGNATURE_HEADER, signature)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    public void registrationFailsWithMissingAppSignatureHeader() {
        webTestClient.get()
                .uri(URL_REGISTER + "?shop-id=shop1&shop-url=https://myshop.de&timestamp=1701363018")
                .header(HttpHeaders.HOST, new TestAppA().getAppKey() + ".app-backend.de")
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    public void registrationFailsWithBlankAppSignature() {
        var app = new TestAppA();
        webTestClient.get()
                .uri(URL_REGISTER + "?shop-id=shop1&shop-url=https://myshop.de&timestamp=1701363018")
                .header(HttpHeaders.HOST, app.getAppKey() + ".app-backend.de")
                .header(SHOPWARE_APP_SIGNATURE_HEADER, "")
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

}
