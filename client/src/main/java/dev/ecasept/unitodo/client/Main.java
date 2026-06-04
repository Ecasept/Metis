package dev.ecasept.unitodo.client;

import dev.ecasept.unitodo.shared.serialization.Serializer;
import dev.ecasept.unitodo.shared.serialization.annotations.Field;
import dev.ecasept.unitodo.shared.serialization.annotations.Serializable;
import dev.ecasept.unitodo.shared.serialization.adapters.LocalDateTimeAdapter;
import dev.ecasept.unitodo.shared.serialization.types.StoreType;

import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        var s = new Serializer().adapter(LocalDateTimeAdapter.class, LocalDateTime.class);
        byte[] serialized = s.serialize(new MockClass());
        // print byte array as hex
        System.out.print("Serialized data: ");
        for (byte b : serialized) {
            System.out.printf("%02x ", b);
        }
        System.out.println();
        System.out.println(s.deserialize(serialized, new StoreType<MockClass>() {}));
    }
}


@Serializable
class MockClass {
    @Field(tag=1)
    private final int a = 1;
    @Field(tag=2, nullable=true)
    public LocalDateTime time = LocalDateTime.now();
    @Field(tag=3, nullable=true)
    public int[] c = null;
}