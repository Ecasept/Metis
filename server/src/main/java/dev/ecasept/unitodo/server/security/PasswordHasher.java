package dev.ecasept.unitodo.server.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class PasswordHasher {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA512";
    private static final int ITERATIONS = 100_000;
    private static final int KEY_LENGTH = 512;
    private static final int SALT_LENGTH = 128/8;

    private static final SecureRandom RANDOM = new SecureRandom();

    public static String hashPassword(dev.ecasept.unitodo.models.Password password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);

        byte[] hash = pbkdf2(password, salt, ITERATIONS, KEY_LENGTH);

        String base64Salt = Base64.getEncoder().encodeToString(salt);
        String base64Hash = Base64.getEncoder().encodeToString(hash);

        return base64Salt + ":" + base64Hash;
    }

    public static boolean verifyPassword(dev.ecasept.unitodo.models.Password password, String storedPasswordHash) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String[] parts = storedPasswordHash.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Stored password hash form is invalid. Expected SALT:HASH");
        }

        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] storedHash = Base64.getDecoder().decode(parts[1]);

        byte[] computedHash = pbkdf2(password, salt, ITERATIONS, KEY_LENGTH);

        return MessageDigest.isEqual(storedHash, computedHash);
    }

    private static byte[] pbkdf2(dev.ecasept.unitodo.models.Password password, byte[] salt, int iterations, int keyLength) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password.pw, salt, iterations, keyLength);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
        return skf.generateSecret(spec).getEncoded();
    }
}

