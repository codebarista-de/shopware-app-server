package de.codebarista.shopware.appserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.Objects;

import static de.codebarista.shopware.appserver.api.ApiConstants.SHOPWARE_APP_SIGNATURE_HEADER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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

    @Test
    public void registerAndConfirmShop() {
        final String shopID = "jmpF3ttj2rEnvmfZ";
        final String shopHost = "my-shop.de";
        final String shopUrl = "https://" + shopHost;

        var app = new TestAppA();
        var registrationResponse = registerShopForApp(webTestClient, app, shopID, shopUrl);
        assertThat(registrationResponse.getConfirmationUrl())
                .endsWith(URL_CONFIRM);
        assertThat(registrationResponse.getShopSecret()).isNotNull();
        String expectedProof = TestHelper.hmac256(shopID + shopUrl + app.getAppName(), app.getAppSecret());
        assertThat(registrationResponse.getProof()).isEqualTo(expectedProof);
        assertThat(registrationResponse.getError()).isNull();

        // Check that the shop was added to the database...
        var shops = shopwareShopEntityRepository.findAll();
        assertThat(shops.size()).isEqualTo(1);
        var shop = shops.get(0);
        assertThat(shop.getAppKey()).isEqualTo(app.getAppKey());
        assertThat(shop.getShopId()).isEqualTo(shopID);
        assertThat(shop.getShopHost()).isEqualTo(shopHost);
        assertThat(shop.getRegistrationConfirmedAt()).isNull();
        // ... and registration is not yet confirmed
        assertThat(shop.getShopSecret()).isEqualTo(registrationResponse.getShopSecret());

        // Send confirmation request
        requestConfirmRegistration(webTestClient, app, registrationResponse.getShopSecret(), shopID, shopUrl).expectStatus().is2xxSuccessful();

        // Check that shop is confirmed in database
        var confirmedShops = shopwareShopEntityRepository.findAll();
        assertThat(confirmedShops.size()).isEqualTo(1);
        var confirmedShop = confirmedShops.get(0);
        assertThat(confirmedShop.getRegistrationConfirmedAt()).isNotNull();
    }

    @Test
    @Sql("/insert_deleted_testshop_for_testapp_a.sql")
    public void reRegisterShopWithSameShopID() {
        webTestClient = webTestClient.mutate().responseTimeout(Duration.ofDays(1)).build(); // TODO: REMOVE

        final String oldShopId = "jmpF3ttj2rEnvmfZ";
        final String newShopHost = "myshopurl-new.test";
        final String newShopUrl = "https://" + newShopHost;

        var app = new TestAppA();
        var registrationResponse = registerShopForApp(webTestClient, app, oldShopId, newShopUrl);

        // Check that the shop-id and app-key have not changed...
        var shops = shopwareShopEntityRepository.findAll();
        assertThat(shops.size()).isEqualTo(1);
        var shop = shops.get(0);
        assertThat(shop.getAppKey()).isEqualTo(app.getAppKey());
        assertThat(shop.getShopId()).isEqualTo(oldShopId);

        // ...but shop-host and shop-url are updated
        assertThat(shop.getShopHost()).isEqualTo(newShopHost);
        assertThat(shop.getShopRequestUrl()).isEqualTo(newShopUrl);

        // ... and confirmation was reset
        assertThat(shop.getRegistrationConfirmedAt()).isNull();
        assertThat(shop.getShopAdminApiKey()).isNull();
        assertThat(shop.getShopAdminApiSecretKey()).isNull();
        assertThat(shop.getShopSecret()).isEqualTo(registrationResponse.getShopSecret());

        // ...and shop is no longer marked as deleted
        assertThat(shop.isMarkedAsDeleted()).isFalse();

        requestConfirmRegistration(webTestClient, app, registrationResponse.getShopSecret(), oldShopId, newShopUrl).expectStatus().is2xxSuccessful();
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

        var confirmedShops = shopwareShopEntityRepository.findAll();
        assertThat(confirmedShops.size()).isEqualTo(1);
        var confirmedShop = confirmedShops.get(0);
        assertThat(confirmedShop.getRegistrationConfirmedAt()).isNull();
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
}
