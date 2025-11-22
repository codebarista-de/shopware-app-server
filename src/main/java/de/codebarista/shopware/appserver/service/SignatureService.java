package de.codebarista.shopware.appserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import de.codebarista.shopware.appserver.exception.InvalidSignatureException;
import de.codebarista.shopware.appserver.exception.SignatureInitializationException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Service for cryptographic signature calculation and verification.
 * <p>
 * This service is automatically configured by {@link de.codebarista.shopware.appserver.config.AppServerServiceAutoConfiguration}.
 * Users can override it by defining their own {@code SignatureService} bean.
 */
public class SignatureService {
    public record SignedResponse(String response, String signature) {
    }

    private static final String SIGNATURE_ALGORITHM = "HmacSHA256";
    private static final String HASH_ALGORITHM = "SHA-256";

    // Thread-safe MessageDigest using ThreadLocal
    private static final ThreadLocal<MessageDigest> HASH_DIGEST =
        ThreadLocal.withInitial(() -> {
            try {
                return MessageDigest.getInstance(HASH_ALGORITHM);
            } catch (NoSuchAlgorithmException e) {
                throw new SignatureInitializationException("Could not initialize hash function SHA-256", e);
            }
        });

    private final ObjectMapper objectMapper;

    public SignatureService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SignedResponse serializeAndCalculateSignature(@Nonnull Object data, @Nonnull String secret) {
        //noinspection ConstantValue
        if (data == null || secret == null) {
            throw new InvalidSignatureException("Data or secret cannot be null");
        }
        try {
            String response = objectMapper.writeValueAsString(data);
            String signature = calculateSignature(response, secret);
            return new SignedResponse(response, signature);
        } catch (JsonProcessingException e) {
            throw new InvalidSignatureException(String.format("Could not calculate signature: %s", e.getMessage()), e);
        }
    }

    public String calculateSignature(@Nonnull String data, @Nonnull String secret) {
        //noinspection ConstantValue
        if (data == null || secret == null) {
            throw new InvalidSignatureException("Data or secret cannot be null");
        }
        try {
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), SIGNATURE_ALGORITHM);
            Mac mac = Mac.getInstance(SIGNATURE_ALGORITHM);
            mac.init(keySpec);
            return bytesToHex(mac.doFinal(data.getBytes()));
        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalArgumentException e) {
            throw new InvalidSignatureException("Could not calculate signature", e);
        }
    }

    public boolean verifySignature(@Nullable byte[] message, @Nullable String secret, @Nullable String signature) {
        if (secret == null || signature == null || message == null) {
            return false;
        }

        try {
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), SIGNATURE_ALGORITHM);
            Mac mac = Mac.getInstance(SIGNATURE_ALGORITHM);
            mac.init(keySpec);
            String calculated = bytesToHex(mac.doFinal(message));
            return signature.equals(calculated);

        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * @return SHA-256 hash
     */
    public String hash(String data) {
        if (data == null) {
            throw new InvalidSignatureException("Data to hash cannot be null");
        }
        final byte[] hashBytes = HASH_DIGEST.get().digest(data.getBytes(StandardCharsets.UTF_8));
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
