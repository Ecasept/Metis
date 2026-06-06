package dev.ecasept.unitodo.shared.models.db;

import java.time.LocalDateTime;
import java.util.function.UnaryOperator;

public class TimestampedField<V> {
    private V value;
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
