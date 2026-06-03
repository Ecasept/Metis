package dev.ecasept.unitodo.models;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class Task {
    private final UUID uuid;
    public TimestampedField<TaskState> state;
    public TimestampedField<Optional<String>> name;
    public TimestampedField<String> description;
    public TimestampedField<LocalDateTime> dueDate;

    public Task() {
        this.uuid = UUID.randomUUID();
    }
}
