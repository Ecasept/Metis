package dev.ecasept.unitodo.server.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class CryptoUtils {
    private static final String ALGORITHM = "HmacSHA256";
    public static byte[] calculateHmac(byte[] data, byte[] secret) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmac = Mac.getInstance(ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret, ALGORITHM);
        hmac.init(secretKeySpec);

        return hmac.doFinal(data);
    }
}
