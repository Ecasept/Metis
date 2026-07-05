package dev.ecasept.unitodo.shared.models.db;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

public interface Task<T> {
    UUID uuid();
    TimestampedField<String> title();
    TimestampedField<String> description();
    TimestampedField<TaskState> state();
    TimestampedField<LocalDate> dueDate();
    TimestampedField<Optional<LocalTime>> dueTime();
    TimestampedField<TaskPriority> priority();
    TimestampedField<Boolean> isDeleted();

    T with(TimestampedField<String> title, TimestampedField<String> description, TimestampedField<TaskState> state, TimestampedField<TaskPriority> priority, TimestampedField<LocalDate> dueDate, TimestampedField<Optional<LocalTime>> dueTime, TimestampedField<Boolean> isDeleted);
}
