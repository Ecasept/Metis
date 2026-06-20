package dev.ecasept.unitodo.shared.models.db;

import dev.ecasept.unitodo.shared.serialization.annotations.Field;
import dev.ecasept.unitodo.shared.serialization.annotations.Serializable;

import java.time.LocalDateTime;
import java.util.function.UnaryOperator;

@Serializable
public class TimestampedField<V> {
    @Field(tag=1)
    private V value;
    @Field(tag=2)
    private LocalDateTime lastUpdated;

    public TimestampedField(V value) {
        this.value = value;
        lastUpdated = LocalDateTime.now();
    }
    public TimestampedField(V value, LocalDateTime lastUpdated) {
        this.value = value;
        this.lastUpdated = lastUpdated;
    }

    public V get() {
        return this.value;
    }

    public void set(V newVal) {
        value = newVal;
        lastUpdated = LocalDateTime.now();
    }

    public void update(UnaryOperator<V> fun) {
        value = fun.apply(value);
        lastUpdated = LocalDateTime.now();
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
}
