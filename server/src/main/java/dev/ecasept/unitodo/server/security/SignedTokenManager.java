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
    public static String generateToken(String payload) throws NoSuchAlgorithmException, InvalidKeyException {
        String encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));

        String signature = calculateHmac(encodedPayload);

        return encodedPayload + "." + signature;
    }

    public static String verifyAndGetPayload(String token) throws NoSuchAlgorithmException, InvalidKeyException {
        String[] parts = token.split("\\.");
        if (parts.length != 2) {
            return null;
        }

        String encodedPayload = parts[0];
        String providedSignature = parts[1];

        String expectedSignature = calculateHmac(encodedPayload);

        if (!MessageDigest.isEqual(providedSignature.getBytes(StandardCharsets.UTF_8), expectedSignature.getBytes(StandardCharsets.UTF_8))) {
            return null;
        }
        byte[] decodedBytes = Base64.getUrlDecoder().decode(encodedPayload);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    private static String calculateHmac(String data) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmac = Mac.getInstance(ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(Configuration.SECRET_KEY, ALGORITHM);
        hmac.init(secretKeySpec);

        byte[] rawHmac = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(rawHmac);
    }

}
