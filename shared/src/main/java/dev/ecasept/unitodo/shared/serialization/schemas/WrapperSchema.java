package dev.ecasept.unitodo.shared.serialization.schemas;

import dev.ecasept.unitodo.shared.serialization.GrowableBuffer;
import dev.ecasept.unitodo.shared.serialization.SerializationException;
import dev.ecasept.unitodo.shared.utils.Log;

import java.nio.ByteBuffer;

/** Includes the instructions for how to (de-)serialize a wrapper type like {@link java.lang.Integer}
 *
 * @param <T> The wrapper type
 */
public class WrapperSchema<T> implements Schema<T> {
    private static final String TAG = "WrapperSerializer";
    private final PrimitiveSchema<T> primitiveSchema;
    private final boolean nullable;
    public WrapperSchema(Class<T> clazz, boolean nullable) {
        this.nullable = nullable;
        this.primitiveSchema = new PrimitiveSchema<>(clazz);
    }

    @Override
    public void serialize(T o, GrowableBuffer buf) {
        Log.i(TAG, "Serializing wrapper object with value: " + o);
        if (nullable) {
            if (serializeNullable(o, buf)) {
                return;
            }
        }
        primitiveSchema.serialize(o, buf);
    }

    @Override
    public T deserialize(ByteBuffer data) throws SerializationException {
        Log.i(TAG, "Deserializing wrapper object");
        if (nullable) {
            if (deserializeNullable(data)) {
                return null;
            }
        }
        var v =  primitiveSchema.deserialize(data);
        Log.i(TAG, "Deserialized wrapper object with value: " + v);
        return v;
    }
}