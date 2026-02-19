package de.codebarista.shopware.appserver;

import de.codebarista.shopware.testutils.TestAppA;
import de.codebarista.shopware.testutils.WebServerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static de.codebarista.shopware.appserver.AppRegistrationApiTest.registerShopForApp;
import static de.codebarista.shopware.appserver.AppRegistrationApiTest.requestConfirmRegistration;
import static de.codebarista.shopware.appserver.AppRegistrationApiTest.requestRegisterShopForApp;
import static de.codebarista.shopware.appserver.AppRegistrationApiTest.requestRegisterShopForAppWithShopSignature;

@WebServerTest
@TestPropertySource(properties = "app-server.enforce-re-registration-with-shop-signature=true")
public class AppRegistrationShopSignatureEnforcedApiTest {
    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void reRegistrationRequiresShopSignatureWhenEnforced() {
        var app = new TestAppA();
        String shopId = "test-shop-sig-enforced-1";
        String shopUrl = "https://myshop.de";

        // Register and confirm
        var registration = registerShopForApp(webTestClient, app, shopId, shopUrl);
        requestConfirmRegistration(webTestClient, app, registration.getShopSecret(), shopId, shopUrl)
                .expectStatus().is2xxSuccessful();

        // Re-register with only app signature (no shop signature) -> expect failure
        requestRegisterShopForApp(webTestClient, app, shopId, shopUrl)
                .expectStatus().isUnauthorized();
    }

    @Test
    public void reRegistrationFailsWithInvalidShopSignatureWhenEnforced() {
        var app = new TestAppA();
        String shopId = "test-shop-sig-enforced-2";
        String shopUrl = "https://myshop.de";

        // Register and confirm
        var registration = registerShopForApp(webTestClient, app, shopId, shopUrl);
        requestConfirmRegistration(webTestClient, app, registration.getShopSecret(), shopId, shopUrl)
                .expectStatus().is2xxSuccessful();

        // Re-register with app signature + INVALID shop signature -> expect failure
        requestRegisterShopForAppWithShopSignature(webTestClient, app, shopId, shopUrl, "wrong-shop-secret")
                .expectStatus().isUnauthorized();
    }
}
