package dev.ecasept.unitodo.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class Task {
    private final UUID uuid;
    private TimestampedField<TaskState> state;
    private TimestampedField<String> name;
    private TimestampedField<String> description;
    private TimestampedField<LocalDateTime> dueDate;

    public Task() {
        this.uuid = UUID.randomUUID();
    }
}
