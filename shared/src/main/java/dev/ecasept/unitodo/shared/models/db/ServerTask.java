package dev.ecasept.unitodo.shared.models.db;

import dev.ecasept.unitodo.shared.serialization.annotations.Field;
import dev.ecasept.unitodo.shared.serialization.annotations.Serializable;
import dev.ecasept.unitodo.shared.utils.DateFormat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Serializable
public record ServerTask(@Field(tag=1) UUID uuid, @Field(tag=2) TimestampedField<String> title, @Field(tag=3) TimestampedField<String> description, @Field(tag=4) TimestampedField<TaskState> state, @Field(tag=5) TimestampedField<TaskPriority> priority, @Field(tag=6) TimestampedField<LocalDateTime> dueDate, @Field(tag=7) TimestampedField<Boolean> isDeleted, @Field(tag=8) UUID userId) {
    public static ServerTask create(String title, String description, TaskState state, TaskPriority priority, LocalDateTime dueDate, boolean isDeleted, UUID userId) {
        return new ServerTask(UUID.randomUUID(),  new TimestampedField<>(title), new TimestampedField<>(description), new TimestampedField<>(state), new TimestampedField<>(priority), new TimestampedField<>(dueDate), new TimestampedField<>(isDeleted), userId);
    }

    public static ServerTask fromResultSet(ResultSet rs) throws SQLException {
        return new ServerTask(
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
                        TaskState.fromInt(rs.getInt("state")),
                        DateFormat.fromLong(rs.getLong("stateChanged"))
                ),
                new TimestampedField<>(
                        TaskPriority.fromInt(rs.getInt("priority")),
                        DateFormat.fromLong(rs.getLong("priorityChanged"))
                ),
                new TimestampedField<>(
                        DateFormat.fromLong(rs.getLong("dueDate")),
                        DateFormat.fromLong(rs.getLong("dueDateChanged"))
                ),
                new TimestampedField<>(
                        rs.getBoolean("isDeleted"),
                        DateFormat.fromLong(rs.getLong("deletedChanged"))
                ),
                UUID.fromString(rs.getString("userId"))
        );
    }

    public static ServerTask fromClientTask(ClientTask task, UUID userId) {
        return new ServerTask(
                task.uuid(),
                task.title(),
                task.description(),
                task.state(),
                task.priority(),
                task.dueDate(),
                task.isDeleted(),
                userId
        );
    }
    public static ClientTask toClientTask(ServerTask task) {
        return new ClientTask(
                task.uuid(),
                task.title(),
                task.description(),
                task.state(),
                task.priority(),
                task.dueDate(),
                task.isDeleted()
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
