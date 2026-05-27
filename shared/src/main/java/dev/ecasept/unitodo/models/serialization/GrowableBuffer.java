package dev.ecasept.unitodo.models.serialization;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


/**
 * A wrapper around ByteBuffer that automatically grows when more capacity is needed.
 */
public class GrowableBuffer {
    /**
     * The buffer that is wrapped
     */
    private ByteBuffer buf;

    /**
     * Creates a new GrowableBuffer with the specified initial capacity and byte order.
     *
     * @param initialCapacity The initial capacity of the buffer
     * @param byteOrder       The byte order of the buffer, e.g. <code>ByteOrder.BIG_ENDIAN</code> or <code>ByteOrder.LITTLE_ENDIAN</code>
     */
    public GrowableBuffer(int initialCapacity, ByteOrder byteOrder) {
        this.buf = ByteBuffer.allocate(initialCapacity).order(byteOrder);
    }

    static int nextPow2(int n) {
        if (n <= 1) return 1;
        return 1 << (32 - Integer.numberOfLeadingZeros(n - 1));
    }

    /**
     * Ensures that the buffer has enough capacity to write the specified number of bytes at the specified index. If not, it grows the buffer by doubling its capacity until it can accommodate the required number of bytes.
     * @param bytes The number of bytes that need to be written to the buffer
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

    /** Ensures that the buffer has enough capacity to write the specified number of bytes at the current position */
    private void ensureBytes(int bytes) {
        ensureBytes(bytes, buf.position());
    }

    /** Wraps <code>ByteBuffer.putInt(int)</code> */
    public void putInt(int value) {
        ensureBytes(4);
        buf.putInt(value);
    }

    /** Wraps <code>ByteBuffer.putInt(int, int)</code> */
    public void putInt(int index, int value) {
        ensureBytes(4, index);
        buf.putInt(index, value);
    }

    /** Wraps <code>ByteBuffer.put(byte)</code> */
    public void putByte(byte b) {
        ensureBytes(1);
        buf.put(b);
    }

    /** Wraps <code>ByteBuffer.put(byte[])</code> */
    public void putBytes(byte[] b) {
        ensureBytes(b.length);
        buf.put(b);
    }

    /** Wraps <code>ByteBuffer.putShort</code> */
    public void putShort(short s) {
        ensureBytes(2);
        buf.putShort(s);
    }

    /** Wraps <code>ByteBuffer.putLong</code> */
    public void putLong(long l) {
        ensureBytes(8);
        buf.putLong(l);
    }

    /** Wraps <code>ByteBuffer.putFloat</code> */
    public void putFloat(float f) {
        ensureBytes(4);
        buf.putFloat(f);
    }

    /** Wraps <code>ByteBuffer.putDouble</code> */
    public void putDouble(double d) {
        ensureBytes(8);
        buf.putDouble(d);
    }

    /** Wraps <code>ByteBuffer.putChar</code> */
    public void putChar(char c) {
        ensureBytes(2);
        buf.putChar(c);
    }

    /** Converts the buffer to a byte array and returns it */
    public byte[] toBytes() {
        buf.flip();
        byte[] out = new byte[buf.limit()];
        buf.get(out);
        return out;
    }

    /** Wraps <code>ByteBuffer.position</code> */
    public int position() {
        return buf.position();
    }
}

