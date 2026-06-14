package dev.ecasept.unitodo.shared.models.api;

import dev.ecasept.unitodo.shared.serialization.annotations.Field;
import dev.ecasept.unitodo.shared.serialization.annotations.Serializable;

import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Serializable
public record Password(@Field(tag = 1) char[] pw) implements AutoCloseable {
    public Password(String pw) {
        this(pw.toCharArray());
    }
    /**
     * Shreds the password by filling the char array with spaces. This removes it from memory.
     */
    public void shred() {
        Arrays.fill(pw, ' ');
    }

    /**
     * Returns the password as a byte array. The password, and the returned byte array, should be shredded after use to remove it from memory.
     * @return the password as a byte array
     */
    public byte[] toBytes() {
        var charBuffer = CharBuffer.wrap(pw);
        var byteBuffer = StandardCharsets.UTF_8.encode(charBuffer);
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);
        Arrays.fill(byteBuffer.array(), (byte) 0);
        return bytes;
    }

    @Override
    public void close() {
        shred();
    }
}
