package de.codebarista.shopware.appserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.codebarista.shopware.appserver.service.SignatureService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class SignatureServiceTest {
    public static final String APP_SECRET = "mysecret";
    private static SignatureService signatureService;

    @BeforeAll
    public static void setup() {
        signatureService = new SignatureService(new ObjectMapper());
    }

    @Test
    public void calculateSignature() {
        String shopId = "zYh6GeKt5VK3UhU5";
        String shopUrl = "http://127.0.0.1:8000";
        String appName = "BaristaUploadApp";
        String dataToSign = shopId + shopUrl + appName;
        String actualProof = signatureService.calculateSignature(dataToSign, APP_SECRET);

        String expectedProof = "7b4ea5e82d2a5405335092141e370f443d0eaa74d2f45c4ddaf4776e7a08aeae";

        assertThat(actualProof).isEqualTo(expectedProof);
    }

    @Test
    public void verifySignature() {
        String shopwareShopSignature = "cc7fa4fb23901eacc5c68ee5a55ca74d1c71264390d7b49c1d86836c039a12c4";
        String query = "shop-id=zYh6GeKt5VK3UhU5&shop-url=http://localhost:8000&timestamp=1703088642"
                + "&sw-version=6.5.7.3&sw-context-language=2fbb5fe2e29a4d70aa5854ce7ce3e20b"
                + "&sw-user-language=en-US";

        assertThat(signatureService.verifySignature(query.getBytes(StandardCharsets.UTF_8), APP_SECRET, shopwareShopSignature)).isTrue();
    }
}
