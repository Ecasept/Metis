package dev.ecasept.unitodo.shared.models.db;

import dev.ecasept.unitodo.shared.serialization.annotations.Field;
import dev.ecasept.unitodo.shared.serialization.annotations.Serializable;
import dev.ecasept.unitodo.shared.utils.DateFormat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

@Serializable
public record ClientTask(@Field(tag=1) UUID uuid, @Field(tag=2) TimestampedField<String> title, @Field(tag=3) TimestampedField<String> description, @Field(tag=4) TimestampedField<TaskState> state, @Field(tag=5) TimestampedField<TaskPriority> priority, @Field(tag=6) TimestampedField<LocalDateTime> dueDate, @Field(tag=7) TimestampedField<Boolean> isDeleted) {
    public static ClientTask create(String title, String description, TaskState state, TaskPriority priority, LocalDateTime dueDate) {
        return new ClientTask(UUID.randomUUID(),  new TimestampedField<>(title), new TimestampedField<>(description), new TimestampedField<>(state), new TimestampedField<>(priority), new TimestampedField<>(dueDate), new TimestampedField<>(false));
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
                )
        );
    }
}
