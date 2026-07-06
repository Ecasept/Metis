package dev.ecasept.unitodo.shared.models.db;

import dev.ecasept.unitodo.shared.serialization.annotations.Field;
import dev.ecasept.unitodo.shared.serialization.annotations.Serializable;

import java.time.LocalDateTime;
import java.util.function.UnaryOperator;

/** Represents a field with an associsated timestamp indicating when it was last updated. Used for conflict resolution during synchronization.
 * @param <V> The type of the value stored in the field
 */
@Serializable
public class TimestampedField<V> {
    /** The value of the field */
    @Field(tag=1)
    private V value;
    /** The timestamp of the last update to the field */
    @Field(tag=2)
    private LocalDateTime lastUpdated;

    /** Provide no-arg constructor for serialization */
    private TimestampedField() {}

    /** Creates a new object with the given value and a modification date of now. */
    public TimestampedField(V value) {
        this.value = value;
        lastUpdated = LocalDateTime.now();
    }

    /** Creates a new object with the given value and modification date. */
    public TimestampedField(V value, LocalDateTime lastUpdated) {
        this.value = value;
        this.lastUpdated = lastUpdated;
    }

    /** Returns the value of the field */
    public V get() {
        return this.value;
    }

    /** Sets the value of the field and updates the lastUpdated timestamp to now. */
    public void set(V newVal) {
        value = newVal;
        lastUpdated = LocalDateTime.now();
    }

    /** Applies the given function to the value of the field and updates the lastUpdated timestamp to now. */
    public void update(UnaryOperator<V> fun) {
        value = fun.apply(value);
        lastUpdated = LocalDateTime.now();
    }

    /** Returns the timestamp of the last update to the field */
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
}
