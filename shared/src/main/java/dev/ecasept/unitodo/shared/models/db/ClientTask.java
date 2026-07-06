package dev.ecasept.unitodo.shared.models.db;

import dev.ecasept.unitodo.shared.serialization.annotations.Field;
import dev.ecasept.unitodo.shared.serialization.annotations.Serializable;
import dev.ecasept.unitodo.shared.utils.DateFormat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

@Serializable
public record ClientTask(@Field(tag=1) UUID uuid, @Field(tag=2) TimestampedField<String> title, @Field(tag=3) TimestampedField<String> description, @Field(tag=4) TimestampedField<TaskState> state, @Field(tag=5) TimestampedField<TaskPriority> priority, @Field(tag=6) TimestampedField<LocalDate> dueDate, @Field(tag=7) TimestampedField<Optional<LocalTime>> dueTime, @Field(tag=8) TimestampedField<Boolean> isDeleted) implements Task<ClientTask> {
    /** Creates a new ClientTask with a random UUID and the current timestamp for all fields. */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static ClientTask create(String title, String description, TaskState state, TaskPriority priority, LocalDate dueDate, Optional<LocalTime> dueTime) {
        return new ClientTask(UUID.randomUUID(),  new TimestampedField<>(title), new TimestampedField<>(description), new TimestampedField<>(state), new TimestampedField<>(priority), new TimestampedField<>(dueDate), new TimestampedField<>(dueTime), new TimestampedField<>(false));
    }

    /** Returns an Optional containing a LocalTime if the column is not null, or an empty Optional if it is null. */
    private static Optional<LocalTime> nullableTime(ResultSet rs, String col) throws SQLException {
        long raw = rs.getLong(col);
        return rs.wasNull() ? Optional.empty() : Optional.of(DateFormat.timeFromLong(raw));
    }

    /** Returns an Optional containing a LocalDateTime if the column is not null, or an empty Optional if it is null. */
    private static Optional<LocalDateTime> nullableDateTime(ResultSet rs, String col) throws SQLException {
        long raw = rs.getLong(col);
        return rs.wasNull() ? Optional.empty() : Optional.of(DateFormat.fromLong(raw));
    }

    /** Creates a ClientTask from a ResultSet obtained from a database query.
     *
     * @param rs The ResultSet to read from
     * @return A ClientTask object populated with the data from the ResultSet
     * @throws SQLException If the ResultSet does not contain the expected columns or if there is an error reading from it
     */
    public static ClientTask fromResultSet(ResultSet rs) throws SQLException {
        return new ClientTask(
                UUID.fromString(rs.getString("uuid")),
                new TimestampedField<>(
                        rs.getString("title"),
                        DateFormat.fromLong(rs.getLong("titleChanged"))
                ),
                new TimestampedField<>(
                        rs.getString("description"),
                        DateFormat.fromLong(rs.getLong("descriptionChanged"))
                ),
                new TimestampedField<>(
                        TaskState.fromInt(rs.getInt("state"), nullableDateTime(rs, "completedAt").orElse(null)),
                        DateFormat.fromLong(rs.getLong("stateChanged"))
                ),
                new TimestampedField<>(
                        TaskPriority.fromInt(rs.getInt("priority")),
                        DateFormat.fromLong(rs.getLong("priorityChanged"))
                ),
                new TimestampedField<>(
                        DateFormat.dateFromLong(rs.getLong("dueDate")),
                        DateFormat.fromLong(rs.getLong("dueDateChanged"))
                ),
                new TimestampedField<>(
                        nullableTime(rs, "dueTime"),
                        DateFormat.fromLong(rs.getLong("dueTimeChanged"))
                ),
                new TimestampedField<>(
                        rs.getBoolean("isDeleted"),
                        DateFormat.fromLong(rs.getLong("deletedChanged"))
                )
        );
    }

    public ClientTask withTitle(String newTitle) {
        return new ClientTask(uuid, new TimestampedField<>(newTitle), description, state, priority, dueDate, dueTime, isDeleted);
    }
    public ClientTask withDescription(String newDescription) {
        return new ClientTask(uuid, title, new TimestampedField<>(newDescription), state, priority, dueDate, dueTime, isDeleted);
    }
    public ClientTask withState(TaskState newState) {
        return new ClientTask(uuid, title, description, new TimestampedField<>(newState), priority, dueDate, dueTime, isDeleted);
    }
    public ClientTask withPriority(TaskPriority newPriority) {
        return new ClientTask(uuid, title, description, state, new TimestampedField<>(newPriority), dueDate, dueTime, isDeleted);
    }
    public ClientTask withDueDate(LocalDate newDueDate) {
        return new ClientTask(uuid, title, description, state, priority, new TimestampedField<>(newDueDate), dueTime, isDeleted);
    }
    public ClientTask withDueTime(@SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<LocalTime> newDueTime) {
        return new ClientTask(uuid, title, description, state, priority, dueDate, new TimestampedField<>(newDueTime), isDeleted);
    }
    public ClientTask withDeleted(boolean newIsDeleted) {
        return new ClientTask(uuid, title, description, state, priority, dueDate, dueTime, new TimestampedField<>(newIsDeleted));
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getTitle() {
        return title.get();
    }

    public String getDescription() {
        return description.get();
    }

    public TaskState getState() {
        return state.get();
    }

    public TaskPriority getPriority() {
        return priority.get();
    }

    public LocalDate getDueDate() {
        return dueDate.get();
    }

    public Optional<LocalTime> getDueTime() {
        return dueTime.get();
    }

    public boolean getDeleted() {
        return isDeleted.get();
    }

    @Override
    public ClientTask with(TimestampedField<String> title, TimestampedField<String> description, TimestampedField<TaskState> state, TimestampedField<TaskPriority> priority, TimestampedField<LocalDate> dueDate, TimestampedField<Optional<LocalTime>> dueTime, TimestampedField<Boolean> isDeleted) {
        return new ClientTask(uuid, title, description, state, priority, dueDate, dueTime, isDeleted);
    }
}
