package dev.ecasept.unitodo.server.security;

import dev.ecasept.unitodo.shared.models.api.Password;
import dev.ecasept.unitodo.server.Configuration;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.nio.ByteBuffer;

public class PasswordHasherService {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA512";
    private static final int ITERATIONS = 100_000;
    private static final int KEY_LENGTH = 512;
    private static final int SALT_LENGTH = 128/8;

    private static final SecureRandom RANDOM = new SecureRandom();

    private final byte[] pepper;

    public PasswordHasherService(Configuration configuration) {
        this.pepper = configuration.PEPPER();
    }

    /** Returns the hash of the specified password */
    public String hashPassword(Password password) {
        try {
            byte[] salt = new byte[SALT_LENGTH];
            RANDOM.nextBytes(salt);

            byte[] hash = hashPasswordWithPepper(password, salt);

            String base64Salt = Base64.getEncoder().encodeToString(salt);
            String base64Hash = Base64.getEncoder().encodeToString(hash);

            return base64Salt + ":" + base64Hash;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException e) {
            throw new RuntimeException("Failed to hash password due to configuration error", e);
        }
    }

    private byte[] hashPasswordWithPepper(Password password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
        var bytePassword = password.toBytes();
        byte[] entry = CryptoUtils.calculateHmac(bytePassword, pepper);
        Arrays.fill(bytePassword, (byte) 0);

        char[] entryChars = toChars(entry);
        byte[] hash = pbkdf2(entryChars, salt, ITERATIONS, KEY_LENGTH);
        Arrays.fill(entryChars, '\0');
        return hash;
    }

    /** Verifies that a provided password matches up with the stored password hash */
    public boolean verifyPassword(Password password, String storedPasswordHash) {
        try {
            String[] parts = storedPasswordHash.split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Stored password hash form is invalid. Expected SALT:HASH");
            }

            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] storedHash = Base64.getDecoder().decode(parts[1]);

            byte[] computedHash = hashPasswordWithPepper(password, salt);

            return MessageDigest.isEqual(storedHash, computedHash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException e) {
            throw new RuntimeException("Failed to verify password due to configuration error", e);
        }
    }

    private byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
        return skf.generateSecret(spec).getEncoded();
    }

    private char[] toChars(byte[] bytes) {
        char[] chars = new char[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            chars[i] = (char) (bytes[i]);
        }
        return chars;
    }
}
