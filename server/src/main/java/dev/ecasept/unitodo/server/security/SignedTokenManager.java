package dev.ecasept.unitodo.server.security;

import dev.ecasept.unitodo.server.Configuration;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class SignedTokenManager {
    private static final String ALGORITHM = "HmacSHA256";
    public static String generateToken(String payload, byte[] secret) {
        try {
            var encodedPayload = Base64.getUrlEncoder().withoutPadding().encode(payload.getBytes(StandardCharsets.UTF_8));

            var signature = calculateHmac(encodedPayload, secret);
            signature = Base64.getUrlEncoder().withoutPadding().encode(signature);

            return new String(encodedPayload, StandardCharsets.UTF_8) + "." + new String(signature, StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Failed to generate token due to configuration error", e);
        }
    }

    public static String verifyAndGetPayload(String token, byte[] secret) {
        if (token == null) {
            return null;
        }
        String[] parts = token.split("\\.");
        if (parts.length != 2) {
            return null;
        }

        try {
            byte[] encodedPayload = parts[0].getBytes(StandardCharsets.UTF_8);
            byte[] providedSignature = parts[1].getBytes(StandardCharsets.UTF_8);
            providedSignature = Base64.getUrlDecoder().decode(providedSignature);

            byte[] expectedSignature = calculateHmac(encodedPayload, secret);

            if (!MessageDigest.isEqual(providedSignature, expectedSignature)) {
                return null;
            }
            byte[] decodedBytes = Base64.getUrlDecoder().decode(encodedPayload);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            // Catches invalid Base64 padding or invalid characters
            return null;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Failed to verify token due to configuration error", e);
        }
    }

    public static byte[] calculateHmac(byte[] data, byte[] secret) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmac = Mac.getInstance(ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret, ALGORITHM);
        hmac.init(secretKeySpec);

        return hmac.doFinal(data);
    }

}
