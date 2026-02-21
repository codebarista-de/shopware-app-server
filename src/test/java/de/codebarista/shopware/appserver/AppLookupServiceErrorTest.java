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
    public void getAppByKeyWithNonExistentKeyThrows() {
        assertThatThrownBy(() -> appLookupService.getAppByKey("nonexistent"))
                .isInstanceOf(NoSuchAppException.class)
                .hasMessage("No such app nonexistent");
    }

    @Test
    public void getAppByKeyWithNullKeyThrows() {
        assertThatThrownBy(() -> appLookupService.getAppByKey(null))
                .isInstanceOf(NoSuchAppException.class)
                .hasMessage("No such app null");
    }

    @Test
    public void getAppByKeyWithEmptyKeyThrows() {
        assertThatThrownBy(() -> appLookupService.getAppByKey(""))
                .isInstanceOf(NoSuchAppException.class)
                .hasMessage("No such app ");
    }

    @Test
    public void getAppByKeyWithValidKeyReturnsCorrectApp() {
        ShopwareApp result = appLookupService.getAppByKey(testAppA.getAppKey());
        assertThat(result).isEqualTo(testAppA);

        result = appLookupService.getAppByKey(testAppB.getAppKey());
        assertThat(result).isEqualTo(testAppB);
    }

    @Test
    public void getAppForHostWithNonExistentHostThrows() {
        // This should extract subdomain and then fail to find app
        assertThatThrownBy(() -> appLookupService.getAppForHost("nonexistent.example.com"))
                .isInstanceOf(NoSuchAppException.class)
                .hasMessage("No such app nonexistent");
    }

    @Test
    public void getAppForHostWithValidHostReturnsCorrectApp() {
        // Assuming testAppA has appKey that matches subdomain
        String hostWithAppKey = testAppA.getAppKey() + ".example.com";
        ShopwareApp result = appLookupService.getAppForHost(hostWithAppKey);
        assertThat(result).isEqualTo(testAppA);
    }

    @Test
    public void tryGetForHostWithNonExistentHostReturnsNull() {
        // tryGetForHost should return null instead of throwing
        ShopwareApp result = appLookupService.tryGetForHost("nonexistent.example.com");
        assertThat(result).isNull();
    }

    @Test
    public void tryGetForHostWithNullHostReturnsNull() {
        // tryGetForHost should handle null gracefully
        ShopwareApp result = appLookupService.tryGetForHost(null);
        assertThat(result).isNull();
    }

    @Test
    public void tryGetForHostWithInvalidHostReturnsNull() {
        // Hosts without dots should return null
        assertThat(appLookupService.tryGetForHost("localhost")).isNull();
        assertThat(appLookupService.tryGetForHost("")).isNull();
        assertThat(appLookupService.tryGetForHost("nodots")).isNull();
    }

    @Test
    public void tryGetForHostWithValidHostReturnsCorrectApp() {
        String hostWithAppKey = testAppA.getAppKey() + ".example.com";
        ShopwareApp result = appLookupService.tryGetForHost(hostWithAppKey);
        assertThat(result).isEqualTo(testAppA);
    }

    @Test
    public void getAppByKeyIsCaseSensitive() {
        String upperCaseKey = testAppA.getAppKey().toUpperCase();

        // Should throw exception because keys are case sensitive
        assertThatThrownBy(() -> appLookupService.getAppByKey(upperCaseKey))
                .isInstanceOf(NoSuchAppException.class);
    }

    @Test
    public void tryGetForHostWithSubdomainOnly() {
        // Test edge case where host is just subdomain + domain
        String minimalHost = testAppA.getAppKey() + ".com";
        ShopwareApp result = appLookupService.tryGetForHost(minimalHost);
        assertThat(result).isEqualTo(testAppA);
    }

    @Test
    public void tryGetForHostWithEmptySubdomainReturnsNull() {
        // Host starting with dot should have empty subdomain
        ShopwareApp result = appLookupService.tryGetForHost(".example.com");
        assertThat(result).isNull();
    }

    @Test
    public void tryGetForHostWithComplexSubdomain_extractsCorrectly() {
        // Should only extract the first part before the first dot
        String complexHost = testAppA.getAppKey() + ".sub.domain.example.com";
        ShopwareApp result = appLookupService.tryGetForHost(complexHost);
        assertThat(result).isEqualTo(testAppA);
    }
}