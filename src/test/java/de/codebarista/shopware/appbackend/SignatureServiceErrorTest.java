package de.codebarista.shopware.appbackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.codebarista.shopware.appbackend.sdk.exception.InvalidSignatureException;
import de.codebarista.shopware.appbackend.sdk.exception.SignatureInitializationException;
import de.codebarista.shopware.appbackend.sdk.service.SignatureService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Error scenario tests for SignatureService focusing on domain-specific exceptions.
 */
public class SignatureServiceErrorTest {

    private SignatureService signatureService;

    @BeforeEach
    public void setup() {
        signatureService = new SignatureService(new ObjectMapper());
    }

    @Test
    public void calculateSignature_withNullData_throwsInvalidSignatureException() {
        assertThatThrownBy(() -> signatureService.calculateSignature(null, "secret"))
                .isInstanceOf(InvalidSignatureException.class)
                .hasMessage("Data or secret cannot be null");
    }

    @Test
    public void calculateSignature_withNullSecret_throwsInvalidSignatureException() {
        assertThatThrownBy(() -> signatureService.calculateSignature("data", null))
                .isInstanceOf(InvalidSignatureException.class)
                .hasMessage("Data or secret cannot be null");
    }

    @Test
    public void calculateSignature_withBothNull_throwsInvalidSignatureException() {
        assertThatThrownBy(() -> signatureService.calculateSignature(null, null))
                .isInstanceOf(InvalidSignatureException.class)
                .hasMessage("Data or secret cannot be null");
    }

    @Test
    public void serializeAndCalculateSignature_withNullData_throwsInvalidSignatureException() {
        assertThatThrownBy(() -> signatureService.serializeAndCalculateSignature(null, "secret"))
                .isInstanceOf(InvalidSignatureException.class)
                .hasMessage("Data or secret cannot be null");
    }

    @Test
    public void serializeAndCalculateSignature_withNullSecret_throwsInvalidSignatureException() {
        Object data = new TestData("test");
        assertThatThrownBy(() -> signatureService.serializeAndCalculateSignature(data, null))
                .isInstanceOf(InvalidSignatureException.class)
                .hasMessage("Data or secret cannot be null");
    }

    @Test
    public void hash_withNullData_throwsInvalidSignatureException() {
        assertThatThrownBy(() -> signatureService.hash(null))
                .isInstanceOf(InvalidSignatureException.class)
                .hasMessage("Data to hash cannot be null");
    }

    @Test
    public void verifySignature_withNullInputs_returnsFalseGracefully() {
        // These should return false instead of throwing exceptions
        assertThat(signatureService.verifySignature(null, "secret", "signature")).isFalse();
        assertThat(signatureService.verifySignature("data".getBytes(), null, "signature")).isFalse();
        assertThat(signatureService.verifySignature("data".getBytes(), "secret", null)).isFalse();
        assertThat(signatureService.verifySignature(null, null, null)).isFalse();
    }

    @Test
    public void verifySignature_withInvalidSignature_returnsFalse() {
        String data = "test data";
        String secret = "test secret";
        String invalidSignature = "invalid_signature";

        assertThat(signatureService.verifySignature(data.getBytes(), secret, invalidSignature)).isFalse();
    }

    @Test
    public void verifySignature_withEmptyInputs_returnsFalse() {
        // Empty secret causes IllegalArgumentException in HMAC, so this should return false gracefully
        assertThat(signatureService.verifySignature("data".getBytes(), "", "signature")).isFalse();
        assertThat(signatureService.verifySignature(new byte[0], "secret", "signature")).isFalse();
    }

    @Test
    public void calculateSignature_withEmptyData_worksCorrectly() {
        // Empty data should work with valid secret
        String result = signatureService.calculateSignature("", "secret");
        assertThat(result).isNotNull().isNotEmpty();
    }

    @Test
    public void calculateSignature_withEmptySecret_throwsInvalidSignatureException() {
        // Empty secret should throw exception due to HMAC requirements
        assertThatThrownBy(() -> signatureService.calculateSignature("data", ""))
                .isInstanceOf(InvalidSignatureException.class)
                .hasMessage("Could not calculate signature");
    }

    @Test
    public void hash_withEmptyString_worksCorrectly() {
        // Empty string should work (not null)
        String result = signatureService.hash("");
        assertThat(result).isNotNull().isNotEmpty();
    }

    @Test
    public void calculateSignature_withVeryLongInputs_worksCorrectly() {
        String longData = "x".repeat(10000);
        String longSecret = "s".repeat(1000);

        String result = signatureService.calculateSignature(longData, longSecret);
        assertThat(result).isNotNull().hasSize(64); // SHA-256 hex string length
    }

    @Test
    public void hash_withUnicodeCharacters_worksCorrectly() {
        String unicodeData = "Hello 世界 🌍 émojis";

        String result = signatureService.hash(unicodeData);
        assertThat(result).isNotNull().hasSize(64); // SHA-256 hex string length
    }

    /**
     * Test data class for serialization tests
     */
    public static class TestData {
        private final String value;

        public TestData(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}