package de.codebarista.shopware.appbackend.sdk.service;

import de.codebarista.shopware.appbackend.sdk.exception.NoSuchAppException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AppLookupService {
    private final Map<String, ShopwareApp> apps;

    public AppLookupService(List<ShopwareApp> apps) {
        this.apps = apps.stream().collect(Collectors.toMap(ShopwareApp::getAppKey, Function.identity()));
    }

    public @NonNull ShopwareApp getAppByKey(String appKey) {
        ShopwareApp app = apps.get(appKey);
        if (app == null) {
            throw NoSuchAppException.byKey(appKey);
        }
        return app;
    }

    public ShopwareApp getAppForHost(String host) {
        String appKey = getSubDomainFromHost(host);
        return getAppByKey(appKey);
    }

    public @Nullable ShopwareApp tryGetForHost(String host) {
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
