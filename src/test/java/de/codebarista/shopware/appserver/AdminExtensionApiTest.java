package de.codebarista.shopware.appserver;

import de.codebarista.shopware.appserver.model.ShopwareShopEntity;
import de.codebarista.shopware.appserver.service.AppLookupService;
import de.codebarista.shopware.appserver.service.ShopManagementService;
import de.codebarista.shopware.testutils.TestAppA;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AdminExtensionApiTest {
    private static final String SHOP_ID = "zYh6GeKt5VK3UhU5";
    private static final String SHOP_HOST = "127.0.0.1:8000";
    private static final String SHOP_URL = "http://127.0.0.1:8000";
    private static final String VERSION = "6.X.X.X";
    private static final String SHOPWARE_SHOP_SIGNATURE =
            "2ad8ce4119d3af34d298358e0e070b5f6e0948c075e9686b225d26276ca54ece";
    private static final String HOST_HEADER_VALUE = TestAppA.APP_KEY + ".app-backend.de";
    private static final String REQUEST_URL = "/shopware/admin/" + TestAppA.ADMIN_EXT_FOLDER + "/v1/index.html?location-id=sw-main-hidden"
            + "&privileges=%7B%22read%22%3A%5B%22order%22%2C%22order_line_item%22%2C%22product%22%2C%22state_machine_state%22%5D%2C%22update%22%3A%5B%22order_line_item%22%5D%7D"
            + "&shop-id=" + SHOP_ID
            + "&shop-url=" + SHOP_URL
            + "&timestamp=1706456295"
            + "&sw-version=6.5.7.3"
            + "&sw-context-language=2fbb5fe2e29a4d70aa5854ce7ce3e20b"
            + "&sw-user-language=de-DE"
            + "&shopware-shop-signature=" + SHOPWARE_SHOP_SIGNATURE;

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ShopManagementService shopManagementService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private AppLookupService appLookupService;

    @Test
    public void successfullyAccessToAdminExtensionUi() {
        String shopSecret = "valid-shop-secret";
        prepareMocks(shopSecret);

        var adminExtensionHtml = webTestClient.get()
                .uri(REQUEST_URL)
                .header(HttpHeaders.HOST, HOST_HEADER_VALUE)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody().returnResult().getResponseBody();

        assertThat(adminExtensionHtml).isNotNull();
        String html = new String(adminExtensionHtml);
        assertThat(html).contains("App A Admin Extension");
    }

    @Test
    public void responseContainsAppTokenInDataSetAttribute() {
        String shopSecret = "valid-shop-secret";
        prepareMocks(shopSecret);

        var adminExtensionHtml = webTestClient.get()
                .uri(REQUEST_URL)
                .header(HttpHeaders.HOST, HOST_HEADER_VALUE)
                .exchange()
                .expectStatus().is2xxSuccessful()
                // The token must not be cached!
                .expectHeader().cacheControl(CacheControl.noStore())
                .expectHeader().valueEquals(HttpHeaders.PRAGMA, "no-cache")
                .expectHeader().valueEquals(HttpHeaders.EXPIRES, "0")
                .expectBody().returnResult().getResponseBody();

        assertThat(adminExtensionHtml).isNotNull();
        String html = new String(adminExtensionHtml);
        Pattern tokenPattern = Pattern.compile("data-token=\"([^\"]+)\"");
        Matcher tokenMatcher = tokenPattern.matcher(html);
        assertThat(tokenMatcher.find()).as("Token data set attribute should be present.").isTrue();
        String token = tokenMatcher.group(1);
        assertThat(token).isNotNull().isNotBlank();
        assertThat(tokenService.isAppTokenValid(appLookupService.getAppByKey(TestAppA.APP_KEY), SHOP_ID, token)).isTrue();
    }

    @Test
    public void responseContainsAppVersionInDataSetAttribute() {
        String shopSecret = "valid-shop-secret";
        prepareMocks(shopSecret);

        var adminExtensionHtml = webTestClient.get()
                .uri(REQUEST_URL)
                .header(HttpHeaders.HOST, HOST_HEADER_VALUE)
                .exchange()
                .expectStatus().is2xxSuccessful()
                // The token must not be cached!
                .expectHeader().cacheControl(CacheControl.noStore())
                .expectHeader().valueEquals(HttpHeaders.PRAGMA, "no-cache")
                .expectHeader().valueEquals(HttpHeaders.EXPIRES, "0")
                .expectBody().returnResult().getResponseBody();

        assertThat(adminExtensionHtml).isNotNull();
        String html = new String(adminExtensionHtml);
        assertThat(html).contains("data-version=\"" + TestAppA.VERSION + "\"");
    }

    @Test
    public void accessToAdminExtensionUiFailsWithInvalidSecret() {
        String shopSecret = "invalid-secret";
        prepareMocks(shopSecret);

        webTestClient.get()
                .uri(REQUEST_URL)
                .header(HttpHeaders.HOST, HOST_HEADER_VALUE)
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    private void prepareMocks(String shopSecret) {
        var shop = new ShopwareShopEntity(TestAppA.APP_KEY, SHOP_ID, SHOP_HOST, SHOP_URL, shopSecret, VERSION);
        when(shopManagementService.getShopById(isA(TestAppA.class), eq(SHOP_ID))).thenReturn(Optional.of(shop));
        when(shopManagementService.getShopByIdOrThrow(isA(TestAppA.class), eq(SHOP_ID))).thenReturn(shop);
    }
}
