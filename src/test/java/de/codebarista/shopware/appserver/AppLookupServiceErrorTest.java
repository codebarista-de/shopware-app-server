package de.codebarista.shopware.appserver;

import de.codebarista.shopware.appserver.exception.NoSuchAppException;
import de.codebarista.shopware.appserver.service.AppLookupService;
import de.codebarista.shopware.testutils.TestAppA;
import de.codebarista.shopware.testutils.TestAppB;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Error scenario tests for AppLookupService focusing on exception handling.
 */
public class AppLookupServiceErrorTest {

    private AppLookupService appLookupService;
    private TestAppA testAppA;
    private TestAppB testAppB;

    @BeforeEach
    public void setup() {
        testAppA = new TestAppA();
        testAppB = new TestAppB();
        List<ShopwareApp> apps = List.of(testAppA, testAppB);
        appLookupService = new AppLookupService(apps);
    }

    @Test
    public void getAppByKey_withNonExistentKey_throwsNoSuchAppException() {
        assertThatThrownBy(() -> appLookupService.getAppByKey("nonexistent"))
                .isInstanceOf(NoSuchAppException.class)
                .hasMessage("No such app nonexistent");
    }

    @Test
    public void getAppByKey_withNullKey_throwsNoSuchAppException() {
        assertThatThrownBy(() -> appLookupService.getAppByKey(null))
                .isInstanceOf(NoSuchAppException.class)
                .hasMessage("No such app null");
    }

    @Test
    public void getAppByKey_withEmptyKey_throwsNoSuchAppException() {
        assertThatThrownBy(() -> appLookupService.getAppByKey(""))
                .isInstanceOf(NoSuchAppException.class)
                .hasMessage("No such app ");
    }

    @Test
    public void getAppByKey_withValidKey_returnsCorrectApp() {
        ShopwareApp result = appLookupService.getAppByKey(testAppA.getAppKey());
        assertThat(result).isEqualTo(testAppA);

        result = appLookupService.getAppByKey(testAppB.getAppKey());
        assertThat(result).isEqualTo(testAppB);
    }

    @Test
    public void getAppForHost_withNonExistentHost_throwsNoSuchAppException() {
        // This should extract subdomain and then fail to find app
        assertThatThrownBy(() -> appLookupService.getAppForHost("nonexistent.example.com"))
                .isInstanceOf(NoSuchAppException.class)
                .hasMessage("No such app nonexistent");
    }

    @Test
    public void getAppForHost_withValidHost_returnsCorrectApp() {
        // Assuming testAppA has appKey that matches subdomain
        String hostWithAppKey = testAppA.getAppKey() + ".example.com";
        ShopwareApp result = appLookupService.getAppForHost(hostWithAppKey);
        assertThat(result).isEqualTo(testAppA);
    }

    @Test
    public void tryGetForHost_withNonExistentHost_returnsNull() {
        // tryGetForHost should return null instead of throwing
        ShopwareApp result = appLookupService.tryGetForHost("nonexistent.example.com");
        assertThat(result).isNull();
    }

    @Test
    public void tryGetForHost_withNullHost_returnsNull() {
        // tryGetForHost should handle null gracefully
        ShopwareApp result = appLookupService.tryGetForHost(null);
        assertThat(result).isNull();
    }

    @Test
    public void tryGetForHost_withInvalidHost_returnsNull() {
        // Hosts without dots should return null
        assertThat(appLookupService.tryGetForHost("localhost")).isNull();
        assertThat(appLookupService.tryGetForHost("")).isNull();
        assertThat(appLookupService.tryGetForHost("nodots")).isNull();
    }

    @Test
    public void tryGetForHost_withValidHost_returnsCorrectApp() {
        String hostWithAppKey = testAppA.getAppKey() + ".example.com";
        ShopwareApp result = appLookupService.tryGetForHost(hostWithAppKey);
        assertThat(result).isEqualTo(testAppA);
    }

    @Test
    public void getAppByKey_isCaseSensitive() {
        String upperCaseKey = testAppA.getAppKey().toUpperCase();

        // Should throw exception because keys are case sensitive
        assertThatThrownBy(() -> appLookupService.getAppByKey(upperCaseKey))
                .isInstanceOf(NoSuchAppException.class);
    }

    @Test
    public void tryGetForHost_withSubdomainOnly_worksCorrectly() {
        // Test edge case where host is just subdomain + domain
        String minimalHost = testAppA.getAppKey() + ".com";
        ShopwareApp result = appLookupService.tryGetForHost(minimalHost);
        assertThat(result).isEqualTo(testAppA);
    }

    @Test
    public void tryGetForHost_withEmptySubdomain_returnsNull() {
        // Host starting with dot should have empty subdomain
        ShopwareApp result = appLookupService.tryGetForHost(".example.com");
        assertThat(result).isNull();
    }

    @Test
    public void tryGetForHost_withComplexSubdomain_extractsCorrectly() {
        // Should only extract the first part before the first dot
        String complexHost = testAppA.getAppKey() + ".sub.domain.example.com";
        ShopwareApp result = appLookupService.tryGetForHost(complexHost);
        assertThat(result).isEqualTo(testAppA);
    }
}