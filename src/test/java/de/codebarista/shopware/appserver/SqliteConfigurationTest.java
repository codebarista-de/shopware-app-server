package de.codebarista.shopware.appserver;

import de.codebarista.shopware.appserver.model.ShopwareShopEntity;
import de.codebarista.shopware.appserver.model.ShopwareShopEntityRepository;
import de.codebarista.shopware.testutils.WebServerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WebServerTest
class SqliteConfigurationTest {
    @Autowired
    private ShopwareShopEntityRepository shopwareShopEntityRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void sqliteForEnabledForeignKeySupport() {
        Integer result = jdbcTemplate.queryForObject("PRAGMA foreign_keys", Integer.class);
        assertThat(result).isEqualTo(1);
    }

    @Test
    void sqliteForBusyTimeout() {
        Integer result = jdbcTemplate.queryForObject("PRAGMA busy_timeout", Integer.class);
        assertThat(result).isEqualTo(5000);
    }

    @Test
    void shopEntityAppKeyAndShopIDUniqueConstraint() {
        ShopwareShopEntity shop = new ShopwareShopEntity("app-key", "shop-id", "myshop.de", "http:://myshop.de", "shop-secret", "6.4.0.0");
        ShopwareShopEntity shopDup = new ShopwareShopEntity("app-key", "shop-id", "staging.myshop.de", "http:://staging.myshop.de", "staging-shop-secret", "6.4.0.0");
        shopwareShopEntityRepository.save(shop);
        assertThatThrownBy(() -> {
            shopwareShopEntityRepository.save(shopDup);
        });
    }
}
