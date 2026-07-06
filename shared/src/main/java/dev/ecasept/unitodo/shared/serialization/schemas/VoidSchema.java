package dev.ecasept.unitodo.shared.serialization.schemas;

import dev.ecasept.unitodo.shared.serialization.GrowableBuffer;
import dev.ecasept.unitodo.shared.utils.Log;

import java.nio.ByteBuffer;

public class VoidSchema implements Schema<Void> {
    private static final String TAG = "VoidSchema";
    @Override
    public void serialize(Void o, GrowableBuffer buf) {
        Log.i(TAG, "Serializing void type");
    }

    @Override
    public Void deserialize(ByteBuffer data) {
        Log.i(TAG, "Deserializing void type");
        return null;
    }
}