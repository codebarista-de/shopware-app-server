package de.codebarista.shopware.testutils;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.fail;

public class TestHelper {
    public static final String PRODUCT_ID = "pr0duct1d";
    public static final String INITIAL_UPLOAD_CODE = "initial-upload-id";
    public static final String ORDER_ID = "0rder1d";

    public static Resource getResource(String filename) {
        try {
            File file = ResourceUtils.getFile("classpath:" + filename);
            InputStream in = new FileInputStream(file);
            return new InputStreamResource(in);
        } catch (FileNotFoundException e) {
            fail("Could not read resource: " + filename);
            return null;
        }
    }

    /**
     * Like {@link #hmac256(byte[], String)} but with UTF-8 encoded data instead of raw bytes
     */
    public static String hmac256(String data, String key) {
        return hmac256(data.getBytes(StandardCharsets.UTF_8), key);
    }

    /**
     * Calculates a HMAC-256 for data with key
     *
     * @param data the data to authenticate
     * @param key  the UTF-8 encoded secret key
     * @return the hex encoded HMAC
     */
    public static String hmac256(byte[] data, String key) {
        try {
            final var keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            final var mac = Mac.getInstance("HmacSHA256");
            mac.init(keySpec);
            return HexFormat.of().formatHex(mac.doFinal(data));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
