package de.codebarista.shopware.appserver;

import de.codebarista.shopware.appserver.api.dto.action.ActionResponseDto;
import de.codebarista.shopware.appserver.api.dto.registration.ShopwareAppRegistrationResponseDto;
import de.codebarista.shopware.testutils.TestAppA;
import de.codebarista.shopware.testutils.TestHelper;
import de.codebarista.shopware.testutils.WebServerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.nio.charset.StandardCharsets;

import static de.codebarista.shopware.appserver.AppRegistrationApiTest.registerShopForApp;
import static de.codebarista.shopware.appserver.AppRegistrationApiTest.requestConfirmRegistration;
import static de.codebarista.shopware.appserver.AppRegistrationApiTest.sendAction;
import static org.assertj.core.api.Assertions.assertThat;

@WebServerTest
public class SignatureVerificationFilterTest {

    private static final String ACTION_URL = "/shopware/api/v1/action";
    private static final String CONFIRM_URL = "/shopware/api/v1/registration/confirm";
    private static final TestAppA APP = new TestAppA();
    private static final String HOST_HEADER = APP.getAppKey() + ".app-backend.de";
    private static final String SHOP_ID = "filter-test-shop";
    private static final String SHOP_URL = "https://myshop.de";

    @Autowired
    private WebTestClient webTestClient;

    private String shopSecret;

    @BeforeEach
    public void setUp() {
        ShopwareAppRegistrationResponseDto registration = registerShopForApp(webTestClient, APP, SHOP_ID, SHOP_URL);
        requestConfirmRegistration(webTestClient, APP, registration.getShopSecret(), SHOP_ID, SHOP_URL)
                .expectStatus().is2xxSuccessful();
        shopSecret = registration.getShopSecret();
    }

    @Test
    public void postSucceedsWithValidShopSignature() {
        ActionResponseDto<?> response = sendAction(webTestClient, APP, shopSecret, SHOP_ID, SHOP_URL, "happy-post")
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(ActionResponseDto.class)
                .returnResult()
                .getResponseBody();
        assertThat(response).extracting("payload.message").isEqualTo("happy-post");
    }

    @Test
    public void getSucceedsWithValidShopSignature() {
        String query = "shop-id=" + SHOP_ID + "&shop-url=" + SHOP_URL + "&timestamp=1706456295";
        String signature = TestHelper.hmac256(query, shopSecret);
        String url = "/shopware/admin/" + TestAppA.ADMIN_EXT_FOLDER + "/v1/index.html?" + query
                + "&shopware-shop-signature=" + signature;

        byte[] body = webTestClient.get()
                .uri(url)
                .header(HttpHeaders.HOST, HOST_HEADER)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .returnResult()
                .getResponseBody();
        assertThat(body).isNotNull();
        assertThat(new String(body)).contains("App A Admin Extension");
    }

    @Test
    public void postFailsWithUnknownHost() {
        byte[] body = String.format("{\"source\":{\"shopId\":\"%s\",\"url\":\"%s\"}}", SHOP_ID, SHOP_URL)
                .getBytes(StandardCharsets.UTF_8);
        String signature = TestHelper.hmac256(body, shopSecret);

        webTestClient.post()
                .uri(ACTION_URL)
                .header(HttpHeaders.HOST, "unknown-app.app-backend.de")
                .header("shopware-shop-signature", signature)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    public void postFailsWithMissingSignatureHeader() {
        byte[] body = String.format("{\"shopId\": \"%s\"}", SHOP_ID).getBytes(StandardCharsets.UTF_8);

        webTestClient.post()
                .uri(CONFIRM_URL)
                .header(HttpHeaders.HOST, HOST_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    public void postFailsWithMalformedJsonBody() {
        byte[] body = "not-json".getBytes(StandardCharsets.UTF_8);
        String signature = TestHelper.hmac256(body, shopSecret);

        webTestClient.post()
                .uri(CONFIRM_URL)
                .header(HttpHeaders.HOST, HOST_HEADER)
                .header("shopware-shop-signature", signature)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    public void postFailsWithJsonArrayBody() {
        byte[] body = "[1, 2, 3]".getBytes(StandardCharsets.UTF_8);
        String signature = TestHelper.hmac256(body, shopSecret);

        webTestClient.post()
                .uri(CONFIRM_URL)
                .header(HttpHeaders.HOST, HOST_HEADER)
                .header("shopware-shop-signature", signature)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    public void postFailsWithMissingShopIdInBody() {
        byte[] body = "{\"foo\": \"bar\"}".getBytes(StandardCharsets.UTF_8);
        String signature = TestHelper.hmac256(body, shopSecret);

        webTestClient.post()
                .uri(CONFIRM_URL)
                .header(HttpHeaders.HOST, HOST_HEADER)
                .header("shopware-shop-signature", signature)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    public void postFailsWithNumericShopIdInRoot() {
        byte[] body = "{\"shopId\": 12345}".getBytes(StandardCharsets.UTF_8);
        String signature = TestHelper.hmac256(body, shopSecret);

        webTestClient.post()
                .uri(CONFIRM_URL)
                .header(HttpHeaders.HOST, HOST_HEADER)
                .header("shopware-shop-signature", signature)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    public void postFailsWithNumericShopIdInSource() {
        byte[] body = "{\"source\":{\"shopId\": 12345}}".getBytes(StandardCharsets.UTF_8);
        String signature = TestHelper.hmac256(body, shopSecret);

        webTestClient.post()
                .uri(ACTION_URL)
                .header(HttpHeaders.HOST, HOST_HEADER)
                .header("shopware-shop-signature", signature)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    public void postFailsWithNonObjectSource() {
        byte[] body = "{\"source\": \"not-an-object\"}".getBytes(StandardCharsets.UTF_8);
        String signature = TestHelper.hmac256(body, shopSecret);

        webTestClient.post()
                .uri(ACTION_URL)
                .header(HttpHeaders.HOST, HOST_HEADER)
                .header("shopware-shop-signature", signature)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    public void postFailsWithSourceMissingShopId() {
        byte[] body = "{\"source\":{\"url\": \"https://myshop.de\"}}".getBytes(StandardCharsets.UTF_8);
        String signature = TestHelper.hmac256(body, shopSecret);

        webTestClient.post()
                .uri(ACTION_URL)
                .header(HttpHeaders.HOST, HOST_HEADER)
                .header("shopware-shop-signature", signature)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    public void postFailsWithUnknownShopId() {
        byte[] body = "{\"shopId\": \"nonexistent-shop\"}".getBytes(StandardCharsets.UTF_8);
        String signature = TestHelper.hmac256(body, shopSecret);

        webTestClient.post()
                .uri(CONFIRM_URL)
                .header(HttpHeaders.HOST, HOST_HEADER)
                .header("shopware-shop-signature", signature)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    public void postFailsWithInvalidSignatureForKnownShop() {
        byte[] body = String.format("{\"source\":{\"shopId\":\"%s\",\"url\":\"%s\"}}", SHOP_ID, SHOP_URL)
                .getBytes(StandardCharsets.UTF_8);

        webTestClient.post()
                .uri(ACTION_URL)
                .header(HttpHeaders.HOST, HOST_HEADER)
                .header("shopware-shop-signature", "invalid-signature")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    public void getFailsWithMissingShopIdParameter() {
        String query = "shop-url=" + SHOP_URL + "&timestamp=1701363018";
        String signature = TestHelper.hmac256(query, shopSecret);

        webTestClient.get()
                .uri("/shopware/api/v1/lifecycle/deleted?" + query + "&shopware-shop-signature=" + signature)
                .header(HttpHeaders.HOST, HOST_HEADER)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    public void getFailsIfOnlyShopSignatureParameterIsPresent() {
        String query = "shop-id=" + SHOP_ID + "&shop-url=" + SHOP_URL + "&timestamp=1706456295";
        String signature = TestHelper.hmac256(query, shopSecret);
        String url = "/shopware/admin/" + TestAppA.ADMIN_EXT_FOLDER + "/v1/index.html?shopware-shop-signature=" + signature;

        webTestClient.get()
                .uri(url)
                .header(HttpHeaders.HOST, HOST_HEADER)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    public void getFailsWithMissingSignatureParameter() {
        webTestClient.get()
                .uri("/shopware/api/v1/lifecycle/deleted?shop-id=" + SHOP_ID)
                .header(HttpHeaders.HOST, HOST_HEADER)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    public void getFailsWithNoQueryString() {
        webTestClient.get()
                .uri("/shopware/api/v1/lifecycle/deleted")
                .header(HttpHeaders.HOST, HOST_HEADER)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    public void getFailsWithUnknownHost() {
        String query = "shop-id=" + SHOP_ID + "&shopware-shop-signature=abc";

        webTestClient.get()
                .uri("/shopware/api/v1/lifecycle/deleted?" + query)
                .header(HttpHeaders.HOST, "unknown-app.app-backend.de")
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    public void getFailsWithUnknownShopId() {
        String query = "shop-id=nonexistent&timestamp=1701363018";
        String signature = TestHelper.hmac256(query, shopSecret);

        webTestClient.get()
                .uri("/shopware/api/v1/lifecycle/deleted?" + query + "&shopware-shop-signature=" + signature)
                .header(HttpHeaders.HOST, HOST_HEADER)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    public void getFailsWithInvalidSignatureForKnownShop() {
        webTestClient.get()
                .uri("/shopware/api/v1/lifecycle/deleted?shop-id=" + SHOP_ID + "&shopware-shop-signature=invalid-sig")
                .header(HttpHeaders.HOST, HOST_HEADER)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    public void unsupportedHttpMethodIsRejected() {
        webTestClient.put()
                .uri(ACTION_URL)
                .header(HttpHeaders.HOST, HOST_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }
}
