package dev.ecasept.unitodo.client;

import dev.ecasept.unitodo.models.serialization.Serializer;
import dev.ecasept.unitodo.models.serialization.annotations.Field;
import dev.ecasept.unitodo.models.serialization.annotations.Serializable;

public class Main {
    public static void main(String[] args) throws IllegalAccessException {
        var s = new Serializer();
        byte[] serialized = s.serialize(new MockClass());
        // print byte array as hex
        System.out.print("Serialized data: ");
        for (byte b : serialized) {
            System.out.printf("%02x ", b);
        }
        System.out.println();
        System.out.println(s.deserialize(serialized, MockClass2.class));
    }
}


@Serializable
class MockClass {
    @Field(tag=1)
    private final int a = 1;
    @Field(tag=2, nullable=true, nullableElements = {false})
    public int[] problematicStrign = {10, 1};
    @Field(tag=3, nullable=true)
    public int[] c = null;
}

@Serializable
class MockClass2 {
    @Field(tag=1)
    private final int a = 1;
    @Field(tag=2, nullable=true, nullableElements = {false, true})
    public int[] asdf = {};
    @Field(tag=3, nullable=true)
    public int[] c = null;
}