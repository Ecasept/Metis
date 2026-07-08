package dev.ecasept.unitodo.server.security;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;

public class SignedTokenService {
    /** Generates a token containing specific payload */
    public String generateToken(String payload, byte[] secret) {
        try {
            var encodedPayload = Base64.getUrlEncoder().withoutPadding().encode(payload.getBytes(StandardCharsets.UTF_8));

            var signature = CryptoUtils.calculateHmac(encodedPayload, secret);
            signature = Base64.getUrlEncoder().withoutPadding().encode(signature);

            return new String(encodedPayload, StandardCharsets.UTF_8) + "." + new String(signature, StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Failed to generate token due to configuration error", e);
        }
    }

    /** Verifies that a token originated from the server and returns the contained payload if it does */
    public Optional<String> verifyAndGetPayload(String token, byte[] secret) {
        if (token == null) {
            return Optional.empty();
        }
        String[] parts = token.split("\\.");
        if (parts.length != 2) {
            return Optional.empty();
        }

        try {
            byte[] encodedPayload = parts[0].getBytes(StandardCharsets.UTF_8);
            byte[] providedSignature = parts[1].getBytes(StandardCharsets.UTF_8);
            providedSignature = Base64.getUrlDecoder().decode(providedSignature);

            byte[] expectedSignature = CryptoUtils.calculateHmac(encodedPayload, secret);

            if (!MessageDigest.isEqual(providedSignature, expectedSignature)) {
                return Optional.empty();
            }
            byte[] decodedBytes = Base64.getUrlDecoder().decode(encodedPayload);
            return Optional.of(new String(decodedBytes, StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            // Catches invalid Base64 padding or invalid characters
            return Optional.empty();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Failed to verify token due to configuration error", e);
        }
    }



}
