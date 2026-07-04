package dev.ecasept.unitodo.shared.serialization.adapters;

import dev.ecasept.unitodo.shared.serialization.GrowableBuffer;
import dev.ecasept.unitodo.shared.serialization.SerializationException;
import dev.ecasept.unitodo.shared.serialization.compilers.DaddyCompiler;
import dev.ecasept.unitodo.shared.serialization.compilers.NullableTypeContainer;
import dev.ecasept.unitodo.shared.serialization.schemas.Schema;
import dev.ecasept.unitodo.shared.utils.Log;

import java.nio.ByteBuffer;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;

public class TemporalAdapter<T extends TemporalAccessor> implements Adapter<T> {
    private static final String TAG = "LocalDateTimeAdapter";
    private final DaddyCompiler daddyCompiler;
    private final TemporalQuery<T> parser;
    private final DateTimeFormatter formatter;

    /**
     * Creates a new adapter for temporal objects
     * @param daddyCompiler The compiler to use for the string representations
     * @param formatter Used for deserializing the string back into the temporal object
     * @param parser Tells the formatter how to query the actual object
     */
    public TemporalAdapter(DaddyCompiler daddyCompiler, DateTimeFormatter formatter, TemporalQuery<T> parser) {
        this.daddyCompiler = daddyCompiler;
        this.parser = parser;
        this.formatter = formatter;
    }

    public TemporalAdapter(DaddyCompiler daddyCompiler, TemporalQuery<T> parser) {
        this(daddyCompiler, DateTimeFormatter.ISO_LOCAL_DATE_TIME, parser);
    }

    @Override
    public Schema<T> compileToSchema(NullableTypeContainer<T> nullableType) {
        return new Schema<>() {
            private final Schema<String> stringSchema = daddyCompiler.compileToSchema(NullableTypeContainer.of(String.class, nullableType.nullable()));

            @Override
            public void serialize(T obj, GrowableBuffer buf) {
                if (nullableType.nullable() && serializeNullable(obj, buf)) {
                    return;
                }
                Log.i(TAG, "Serializing temporal: " + obj.toString());
                String s = formatter.format(obj);
                stringSchema.serialize(s, buf);
            }

            @Override
            public T deserialize(ByteBuffer data) throws SerializationException {
                if (nullableType.nullable() && deserializeNullable(data)) {
                    return null;
                }
                String s = stringSchema.deserialize(data);
                Log.i(TAG, "Deserialized temporal string: " + s);
                try {
                    return formatter.parse(s, parser);
                } catch (DateTimeParseException e) {
                    throw new SerializationException("Failed to parse LocalDateTime from string: " + s, e);
                }
            }
        };
    }
}
