package de.codebarista.shopware.appserver.service;

import de.codebarista.shopware.appserver.ShopwareApp;
import de.codebarista.shopware.appserver.exception.NoSuchAppException;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service for looking up ShopwareApp instances by key or host.
 * <p>
 * This service is automatically configured by {@link de.codebarista.shopware.appserver.config.AppServerServiceAutoConfiguration}.
 * Users can override it by defining their own {@code AppLookupService} bean.
 */
public class AppLookupService {
    private final Map<String, ShopwareApp> apps;

    public AppLookupService(List<ShopwareApp> apps) {
        this.apps = apps.stream().collect(Collectors.toMap(ShopwareApp::getAppKey, Function.identity()));
    }

    @Nonnull public ShopwareApp getAppByKey(@Nonnull String appKey) {
        ShopwareApp app = apps.get(appKey);
        if (app == null) {
            throw NoSuchAppException.byKey(appKey);
        }
        return app;
    }

    @Nonnull public ShopwareApp getAppForHost(@Nonnull String host) {
        String appKey = getSubDomainFromHost(host);
        return getAppByKey(appKey);
    }

    @Nullable public ShopwareApp tryGetForHost(@Nullable String host) {
        String appKey = getSubDomainFromHost(host);
        return apps.get(appKey);
    }

    private String getSubDomainFromHost(String host) {
        if (host == null || !host.contains(".")) {
            return null;
        }
        String subdomain = host.substring(0, host.indexOf('.'));
        return subdomain.isBlank() ? null : subdomain;
    }
}
