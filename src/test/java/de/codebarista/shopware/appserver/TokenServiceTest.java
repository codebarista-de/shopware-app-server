package de.codebarista.shopware.appserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.codebarista.shopware.appserver.model.ShopwareShopEntity;
import de.codebarista.shopware.appserver.service.ShopManagementService;
import de.codebarista.shopware.appserver.service.SignatureService;
import de.codebarista.shopware.appserver.service.TokenServiceImpl;
import de.codebarista.shopware.testutils.TestAppA;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TokenServiceTest {

    @Test
    public void generateAndValidateAppToken() {
        TestAppA electronicInvoicingApp = new TestAppA();

        var shopManagementService = mock(ShopManagementService.class);
        String shopId = "shopId";
        var shopEntity = new ShopwareShopEntity("appKey", shopId);
        shopEntity.setPendingRegistration("testSecret", "https://my-shop.de");
        shopEntity.confirmPendingRegistrationAndAddShopApiSecrets("apiKey", "apiSecret");
        when(shopManagementService.getShopByIdOrThrow(any(), any())).thenReturn(shopEntity);
        when(shopManagementService.getShopById(any(), any())).thenReturn(Optional.of(shopEntity));

        TokenServiceImpl tokenService = new TokenServiceImpl(shopManagementService, new SignatureService(new ObjectMapper()));

        String token = tokenService.generateAppToken(electronicInvoicingApp, shopId);
        assertThat(token).isNotNull();
        assertThat(token).hasSize(148); // 20 chars timestamp + 64 chars hash + 64 chars signature

        assertThat(tokenService.isAppTokenValid(electronicInvoicingApp, shopId, token)).isTrue();
    }
}
