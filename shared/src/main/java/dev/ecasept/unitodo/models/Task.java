package dev.ecasept.unitodo.models;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class Task {
    private final UUID uuid;
    private TimestampedField<TaskState> state;
    private TimestampedField<TaskPriority> priority;
    private TimestampedField<String> name;
    private TimestampedField<String> description;
    private TimestampedField<LocalDateTime> dueDate;

    public Task(String name) {
        this.uuid = UUID.randomUUID();
        setName(name);
        setState(TaskState.Pending);
        setPriority(TaskPriority.Mid); // default priority
    }

    public Task(String name, String description) {
        this(name);
        setDescription(description);
    }

    public Task(String name, String description, TaskPriority priority) {
        this(name, description);
        setPriority(priority);
    }

    public Task(String name, String description, LocalDateTime dueDate) {
        this(name, description);
        setDueDate(dueDate);
    }

    public Task(String name, String description, LocalDateTime dueDate, TaskPriority priority) {
        this(name, description, dueDate);
        setPriority(priority);
    }

    public Task(String name, LocalDateTime dueDate) {
        this(name);
        setDueDate(dueDate);
    }

    public Task(String name, LocalDateTime dueDate, TaskPriority priority) {
        this(name, dueDate);
        setPriority(priority);
    }

    /* getter */
    public String getName() {
        return name.get();
    }

    public TaskState getState() {
        return state.get();
    }

    public TaskPriority getPriority() {
        return priority.get();
    }

    public Optional<String> getDescription() {
        if (description == null || description.get() == null) {
            return Optional.empty();
        }
        return Optional.of(description.get());
    }

    public Optional<LocalDateTime> getDueDate() {
        if (dueDate == null || dueDate.get() == null) {
            return Optional.empty();
        }
        return Optional.of(dueDate.get());
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public LocalDateTime getLastUpdate() {
        return Stream.of(name, description, dueDate, state, priority)
                .filter(Objects::nonNull)
                .map(TimestampedField::getLastUpdated)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    /* setter */
    public void setName(String name) {
        if (this.name == null)
            this.name = new TimestampedField<>(name);
        else
            this.name.set(name);
    }

    public void setState(TaskState state) {
        if (this.state == null)
            this.state = new TimestampedField<>(state);
        else
            this.state.set(state);
    }

    public void setDescription(String description) {
        if (this.description == null)
            this.description = new TimestampedField<>(description);
        else
            this.description.set(description);
    }

    public void setDueDate(LocalDateTime dueDate) {
        if (this.dueDate == null)
            this.dueDate = new TimestampedField<>(dueDate);
        else
            this.dueDate.set(dueDate);
    }

    public void setPriority(TaskPriority priority) {
        if (this.priority == null)
            this.priority = new TimestampedField<>(priority);
        else
            this.priority.set(priority);
    }
}
