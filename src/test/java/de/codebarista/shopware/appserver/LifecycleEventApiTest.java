package de.codebarista.shopware.appserver;

import de.codebarista.shopware.appserver.api.dto.event.ShopwareEventDataDto;
import de.codebarista.shopware.appserver.api.dto.event.ShopwareEventDto;
import de.codebarista.shopware.appserver.api.dto.event.ShopwareEventSourceDto;
import de.codebarista.shopware.appserver.model.ShopwareShopEntity;
import de.codebarista.shopware.appserver.model.ShopwareShopEntityRepository;
import de.codebarista.shopware.testutils.TestAppA;
import de.codebarista.shopware.testutils.WebServerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@WebServerTest
public class LifecycleEventApiTest {
    private static final String HOST_HEADER_VALUE = TestAppA.APP_KEY + ".app-backend.de";
    private static final String URL_DELETED = "/shopware/api/v1/lifecycle/deleted";
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private ShopwareShopEntityRepository shopwareShopEntityRepository;

    @Test
    public void testActivated() {
        // TODO: implement. Event not yet used.
    }

    @Test
    public void testDeactivated() {
        // TODO: implement. Event not yet used.
    }

    @Test
    public void testUpdated() {
        // TODO: implement. Event not yet used.
    }

    @Test
    @Sql("/insert_testshop_for_testapp_a.sql")
    public void successfullyDeleteShop() {
        String shopId = "test1234";
        String shopUrl = "http://myshopurl.test";
        String shopHost = "myshopurl.test";

        webTestClient.post()
                .uri(URL_DELETED)
                .header(HttpHeaders.HOST, HOST_HEADER_VALUE)
                .header("shopware-shop-signature",
                        "ca02cd7c8eff3ec0502239b05c089fd5510a593dfabdc7106caf03d7f3a2cbd2")
                .contentType(MediaType.APPLICATION_JSON)
                // use hardcoded JSON value to prevent test failures after dependency upgrades due to changes in the property serialization order
                .bodyValue("{\"timestamp\":\"1702212669\",\"data\":{\"payload\":[],\"event\":\"app.deleted\"},\"source\":{\"appVersion\":\"0.0.1\",\"shopId\":\"test1234\",\"eventId\":\"e4ent1d\",\"url\":\"http://myshopurl.test\"}}")
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        var shop = checkAndReturnShopEntity(shopId, shopHost);
        assertThat(shop.getRegistrationConfirmedAt()).isNotNull();
        assertThat(shop.getDeletedAt()).isNotNull();
    }

    @Test
    @Sql("/insert_testshop_for_testapp_a.sql")
    public void shopDeletionFailsWithInvalidSignature() {
        String shopId = "test1234";
        String shopUrl = "http://myshopurl.test";
        String shopHost = "myshopurl.test";
        var deletedEventDto = buildDeletedEventDto(shopUrl, shopId);

        String signature = "invalid-shop-signature";
        webTestClient.post()
                .uri(URL_DELETED)
                .header(HttpHeaders.HOST, HOST_HEADER_VALUE)
                .header("shopware-shop-signature", signature)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(deletedEventDto)
                .exchange()
                .expectStatus()
                .is4xxClientError();

        var shop = checkAndReturnShopEntity(shopId, shopHost);
        assertThat(shop.getDeletedAt()).isNull();
    }

    private ShopwareEventDto buildDeletedEventDto(String shopUrl, String shopId) {
        String appVersion = "0.0.1";
        String eventId = "e4ent1d";
        var source = new ShopwareEventSourceDto(shopUrl, appVersion, shopId, eventId);
        var data = new ShopwareEventDataDto(Collections.emptyList(), "app.deleted");
        String timestamp = "1702212669";
        return new ShopwareEventDto(timestamp, data, source);
    }

    private ShopwareShopEntity checkAndReturnShopEntity(String shopId, String shopHost) {
        var shops = shopwareShopEntityRepository.findAll();
        assertThat(shops.size()).isEqualTo(1);
        var shop = shops.get(0);
        assertThat(shop.getAppKey()).isEqualTo(TestAppA.APP_KEY);
        assertThat(shop.getShopId()).isEqualTo(shopId);
        assertThat(shop.getShopHost()).isEqualTo(shopHost);
        return shop;
    }
}
