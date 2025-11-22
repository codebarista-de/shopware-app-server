package de.codebarista.shopware.appserver.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShopwareShopEntityRepository extends JpaRepository<ShopwareShopEntity, Long> {
    Optional<ShopwareShopEntity> findByAppKeyAndShopId(String appKey, String shopId);

    List<ShopwareShopEntity> findByAppKeyAndShopHost(String appKey, String shopHost);
}
