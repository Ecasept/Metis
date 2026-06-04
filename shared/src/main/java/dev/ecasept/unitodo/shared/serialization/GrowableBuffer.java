package dev.ecasept.unitodo.shared.serialization;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A wrapper around {@link ByteBuffer} that automatically grows when more capacity is needed.
 */
public class GrowableBuffer {
    /**
     * The buffer that is wrapped.
     */
    private ByteBuffer buf;

    /**
     * Creates a new {@code GrowableBuffer} with the specified initial capacity and byte order.
     *
     * @param initialCapacity The initial capacity of the buffer.
     * @param byteOrder       The byte order of the buffer, e.g. {@link ByteOrder#BIG_ENDIAN} or {@link ByteOrder#LITTLE_ENDIAN}.
     */
    public GrowableBuffer(int initialCapacity, ByteOrder byteOrder) {
        this.buf = ByteBuffer.allocate(initialCapacity).order(byteOrder);
    }

    static int nextPow2(int n) {
        if (n <= 1) return 1;
        return 1 << (32 - Integer.numberOfLeadingZeros(n - 1));
    }

    /**
     * Ensures that the buffer has enough capacity to write the specified number of bytes at the specified index.
     * If not, it grows the buffer by doubling its capacity until it can accommodate the required number of bytes.
     *
     * @param bytes The number of bytes that need to be written to the buffer.
     * @param pos   The position at which the bytes will be written.
     */
    private void ensureBytes(int bytes, int pos) {
        int required = pos + bytes;
        if (required <= buf.capacity()) {
            return;
        }
        var newCapacity = nextPow2(required);
        ByteBuffer newBuf = ByteBuffer.allocate(newCapacity).order(buf.order());
        buf.flip();
        newBuf.put(buf);
        buf = newBuf;
    }

    /**
     * Ensures that the buffer has enough capacity to write the specified number of bytes at the current position.
     *
     * @param bytes The number of bytes that need to be written to the buffer.
     */
    private void ensureBytes(int bytes) {
        ensureBytes(bytes, buf.position());
    }

    /**
     * Wraps {@link ByteBuffer#putInt(int)}.
     *
     * @param value The integer value to put.
     */
    public void putInt(int value) {
        ensureBytes(4);
        buf.putInt(value);
    }

    /**
     * Wraps {@link ByteBuffer#putInt(int, int)}.
     *
     * @param index The index at which the integer will be written.
     * @param value The integer value to put.
     */
    public void putInt(int index, int value) {
        ensureBytes(4, index);
        buf.putInt(index, value);
    }

    /**
     * Wraps {@link ByteBuffer#put(byte)}.
     *
     * @param b The byte value to put.
     */
    public void putByte(byte b) {
        ensureBytes(1);
        buf.put(b);
    }

    /**
     * Wraps {@link ByteBuffer#put(byte[])}.
     *
     * @param b The byte array to put.
     */
    public void putBytes(byte[] b) {
        ensureBytes(b.length);
        buf.put(b);
    }

    /**
     * Wraps {@link ByteBuffer#putShort(short)}.
     *
     * @param s The short value to put.
     */
    public void putShort(short s) {
        ensureBytes(2);
        buf.putShort(s);
    }

    /**
     * Wraps {@link ByteBuffer#putLong(long)}.
     *
     * @param l The long value to put.
     */
    public void putLong(long l) {
        ensureBytes(8);
        buf.putLong(l);
    }

    /**
     * Wraps {@link ByteBuffer#putFloat(float)}.
     *
     * @param f The float value to put.
     */
    public void putFloat(float f) {
        ensureBytes(4);
        buf.putFloat(f);
    }

    /**
     * Wraps {@link ByteBuffer#putDouble(double)}.
     *
     * @param d The double value to put.
     */
    public void putDouble(double d) {
        ensureBytes(8);
        buf.putDouble(d);
    }

    /**
     * Wraps {@link ByteBuffer#putChar(char)}.
     *
     * @param c The boolean character to put.
     */
    public void putChar(char c) {
        ensureBytes(2);
        buf.putChar(c);
    }

    /**
     * Converts the buffer to a byte array and returns it.
     *
     * @return The buffer contents as a newly allocated byte array.
     */
    public byte[] toBytes() {
        buf.flip();
        byte[] out = new byte[buf.limit()];
        buf.get(out);
        return out;
    }

    /**
     * Wraps {@link ByteBuffer#position()}.
     *
     * @return The current position of the buffer.
     */
    public int position() {
        return buf.position();
    }
}