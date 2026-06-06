package dev.ecasept.unitodo.shared.models.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

public record Task(UUID uuid, TimestampedField<String> title, TimestampedField<String> description, TimestampedField<TaskState> state, TimestampedField<TaskPriority> priority, TimestampedField<LocalDateTime> dueDate, boolean isDeleted, LocalDateTime deletedAt) {
    public static Task create(String title, String description, TaskState state, TaskPriority priority, LocalDateTime dueDate) {
        return new Task(UUID.randomUUID(),  new TimestampedField<>(title), new TimestampedField<>(description), new TimestampedField<>(state), new TimestampedField<>(priority), new TimestampedField<>(dueDate), false, null);
    }

    private static LocalDateTime dateParse(long epoch) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneOffset.UTC);
    }
    public static Task fromResultSet(ResultSet rs) throws SQLException {
        var isDeleted = rs.getBoolean("isDeleted");
        return new Task(
                UUID.fromString(rs.getString("uuid")),
                new TimestampedField<>(
                        rs.getString("title"),
                        dateParse(rs.getLong("titleChanged"))
                ),
                new TimestampedField<>(
                        rs.getString("description"),
                        dateParse(rs.getLong("descriptionChanged"))
                ),
                new TimestampedField<>(
                        TaskState.fromInt(rs.getInt("state")),
                        dateParse(rs.getLong("stateChanged"))
                ),
                new TimestampedField<>(
                        TaskPriority.fromInt(rs.getInt("priority")),
                        dateParse(rs.getLong("priorityChanged"))
                ),
                new TimestampedField<>(
                        dateParse(rs.getLong("dueDate")),
                        dateParse(rs.getLong("dueDateChanged"))
                ),
                isDeleted,
                isDeleted ? dateParse(rs.getLong("deletedAt")) : null
        );
    }

    public LocalDateTime getLastUpdate() {
        return Stream.of(title, description, state, priority, dueDate)
                .map(TimestampedField::getLastUpdated)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.MIN);
    }
}
