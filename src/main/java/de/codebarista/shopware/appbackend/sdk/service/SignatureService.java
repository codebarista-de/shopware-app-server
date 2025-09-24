package de.codebarista.shopware.appbackend.sdk.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component // TODO: convert to helper class with static methods?
public class SignatureService {
    public record SignedResponse(String response, String signature) {
    }

    private static final String SIGNATURE_ALGORITHM = "HmacSHA256";
    private static final String HASH_ALGORITHM = "SHA-256";

    private final ObjectMapper objectMapper; // TODO: use service-global ObjectMapper?
    private final MessageDigest hashDigest;

    public SignatureService() {
        objectMapper = new ObjectMapper();
        try {
            hashDigest = MessageDigest.getInstance(HASH_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Could not initialize hash function SHA3-256", e);
        }
    }

    public SignedResponse serializeAndCalculateSignature(@Nonnull Object data, @Nonnull String secret) {
        //noinspection ConstantValue
        if (data == null || secret == null) {
            throw new RuntimeException("Data or secret cannot be null");
        }
        try {
            String response = objectMapper.writeValueAsString(data);
            String signature = calculateSignature(response, secret);
            return new SignedResponse(response, signature);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(String.format("Could not calculate signature: %s", e.getMessage()), e);
        }
    }

    public String calculateSignature(@Nonnull String data, @Nonnull String secret) {
        //noinspection ConstantValue
        if (data == null || secret == null) {
            throw new RuntimeException("Data or secret cannot be null");
        }
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), SIGNATURE_ALGORITHM);
        try {
            Mac mac = Mac.getInstance(SIGNATURE_ALGORITHM);
            mac.init(keySpec);
            return bytesToHex(mac.doFinal(data.getBytes()));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Could not calculate signature", e);
        }
    }

    public boolean verifySignature(@Nullable byte[] message, @Nullable String secret, @Nullable String signature) {
        if (secret == null || signature == null || message == null) {
            return false;
        }

        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), SIGNATURE_ALGORITHM);
        try {
            Mac mac = Mac.getInstance(SIGNATURE_ALGORITHM);
            mac.init(keySpec);
            String calculated = bytesToHex(mac.doFinal(message));
            return signature.equals(calculated);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            return false;
        }
    }

    /**
     * @return SHA-256 hash
     */
    public String hash(String data) {
        if (data == null) {
            throw new RuntimeException("Data to hash cannot be null");
        }
        final byte[] hashBytes = hashDigest.digest(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hashBytes);
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
