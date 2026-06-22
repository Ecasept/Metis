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
public record ClientTask(@Field(tag=1) UUID uuid, @Field(tag=2) TimestampedField<String> title, @Field(tag=3) TimestampedField<String> description, @Field(tag=4) TimestampedField<TaskState> state, @Field(tag=5) TimestampedField<TaskPriority> priority, @Field(tag=6) TimestampedField<LocalDate> dueDate, @Field(tag=7) TimestampedField<Optional<LocalTime>> dueTime, @Field(tag=8) TimestampedField<Boolean> isDeleted) {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static ClientTask create(String title, String description, TaskState state, TaskPriority priority, LocalDate dueDate, Optional<LocalTime> dueTime) {
        return new ClientTask(UUID.randomUUID(),  new TimestampedField<>(title), new TimestampedField<>(description), new TimestampedField<>(state), new TimestampedField<>(priority), new TimestampedField<>(dueDate), new TimestampedField<>(dueTime), new TimestampedField<>(false));
    }

    private static Optional<LocalTime> nullableTime(ResultSet rs, String col) throws SQLException {
        long raw = rs.getLong(col);
        return rs.wasNull() ? Optional.empty() : Optional.of(DateFormat.timeFromLong(raw));
    }

    private static Optional<LocalDateTime> nullableDateTime(ResultSet rs, String col) throws SQLException {
        long raw = rs.getLong(col);
        return rs.wasNull() ? Optional.empty() : Optional.of(DateFormat.fromLong(raw));
    }

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
}
