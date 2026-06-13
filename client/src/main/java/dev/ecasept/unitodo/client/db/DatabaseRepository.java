package dev.ecasept.unitodo.client.db;

import dev.ecasept.unitodo.shared.db.DatabaseController;
import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.db.SortOrder;
import dev.ecasept.unitodo.shared.models.db.Task;
import dev.ecasept.unitodo.shared.models.db.TaskState;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;


public class DatabaseRepository {
    private final DatabaseController controller;

    private long dateFormat(LocalDateTime time) {
        return time.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public DatabaseRepository(DatabaseController controller) {
        this.controller = controller;
    }

    public Optional<Task> getTask(String uuid) throws DatabaseException {
        try (var statement = controller.prepareStatement("SELECT * FROM tasks WHERE uuid == ?")) {
            statement.setString(1, uuid);
            try (var rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(Task.fromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to get task from database", e);
        }
    }

    public ArrayList<Task> getTasks(List<UUID> uuids) throws DatabaseException {
        var sb = new StringBuilder();
        sb.append("SELECT * from tasks WHERE uuid IN (");
        sb.repeat("?, ", uuids.size() - 1).append("?);");
        try (var statement = controller.prepareStatement(sb.toString())) {
            for (int i = 0; i < uuids.size(); i++) {
                statement.setString(i, uuids.get(i).toString());
            }
            var rs = statement.executeQuery();
            var tasks = new ArrayList<Task>();
            while (rs.next()) {
                tasks.add(Task.fromResultSet(rs));
            }
            return tasks;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to get tasks from database", e);
        }
    }

    public ArrayList<Task> searchTasks(String search) throws DatabaseException {
        var string = "SELECT * from tasks WHERE title LIKE ? OR description LIKE ?";
        try (var statement = controller.prepareStatement(string)) {
            statement.setString(1, "%" + search + "%");
            statement.setString(2, "%" + search + "%");
            var rs = statement.executeQuery();
            var tasks = new ArrayList<Task>();
            while (rs.next()) {
                tasks.add(Task.fromResultSet(rs));
            }
            return tasks;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to search tasks in database", e);
        }
    }

    public ArrayList<Task> getTasks(TaskState state, SortOrder order) throws DatabaseException {
        var str = "SELECT * from tasks WHERE state = ? ORDER BY dueDate " + order.asSql() + ";";
        try (var statement = controller.prepareStatement(str)) {
            statement.setInt(1, state.toInt());
            var rs = statement.executeQuery();
            var tasks = new ArrayList<Task>();
            while (rs.next()) {
                tasks.add(Task.fromResultSet(rs));
            }
            return tasks;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to get tasks from database", e);
        }
    }

    public ArrayList<Task> getTasks(SortOrder order) throws DatabaseException {
        var str = "SELECT * from tasks ORDER BY dueDate " + order.asSql() + ";";
        try (var statement = controller.prepareStatement(str)) {
            var rs = statement.executeQuery();
            var tasks = new ArrayList<Task>();
            while (rs.next()) {
                tasks.add(Task.fromResultSet(rs));
            }
            return tasks;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to get tasks from database", e);
        }
    }

    public void upsertTask(Task task) throws DatabaseException {
        upsertTasks(List.of(task));
    }

    public void upsertTasks(List<Task> tasks) throws DatabaseException {
        var str = """
        INSERT INTO tasks (
            uuid, title, description, state, priority, dueDate, titleChanged, descriptionChanged, stateChanged, priorityChanged, dueDateChanged, isDeleted, deletedAt
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ON CONFLICT(uuid)
        DO UPDATE SET
            title = excluded.title,
            description = excluded.description,
            state = excluded.state,
            priority = excluded.priority,
            dueDate = excluded.dueDate,
            titleChanged = excluded.titleChanged,
            descriptionChanged = excluded.descriptionChanged,
            stateChanged = excluded.stateChanged,
            priorityChanged = excluded.priorityChanged,
            dueDateChanged = excluded.dueDateChanged,
            isDeleted = excluded.isDeleted,
            deletedAt = excluded.deletedAt;
        """;

        try (var statement = controller.prepareStatement(str)) {
            for (var task : tasks) {
                statement.setString(1, task.uuid().toString());
                statement.setString(2, task.title().get());
                statement.setString(3, task.description().get());
                statement.setInt(4, task.state().get().toInt());
                statement.setInt(5, task.priority().get().toInt());
                statement.setLong(6, dateFormat(task.dueDate().get()));
                statement.setLong(7, dateFormat(task.title().getLastUpdated()));
                statement.setLong(8, dateFormat(task.description().getLastUpdated()));
                statement.setLong(9, dateFormat(task.state().getLastUpdated()));
                statement.setLong(10, dateFormat(task.priority().getLastUpdated()));
                statement.setLong(11, dateFormat(task.dueDate().getLastUpdated()));
                statement.setBoolean(12, task.isDeleted());
                statement.setLong(13, task.deletedAt() != null ? dateFormat(task.deletedAt()) : 0);
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to store new task", e);
        }
    }
    public boolean deleteTask(String uuid) throws DatabaseException {
        var str = "DELETE FROM tasks WHERE uuid == ?";
        try (var statement = controller.prepareStatement(str)) {
            statement.setString(1, uuid);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete task", e);
        }
    }

    private String getConstant(String key) throws DatabaseException, SQLException {
        try (var statement = controller.prepareStatement("SELECT value FROM constants WHERE key == ?")) {
            statement.setString(1, key);
            try (var rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return rs.getString("value");
            }
        }
    }
    private void setConstant(String key, String value) throws DatabaseException, SQLException {
        var str = """
        INSERT INTO constants (key, value) VALUES (?, ?)
        ON CONFLICT(key) DO UPDATE SET value = excluded.value
        """;
        try (var statement = controller.prepareStatement(str)) {
            statement.setString(1, key);
            statement.setString(2, value);
            statement.executeUpdate();
        }
    }

    public String getSessionToken() throws DatabaseException {
        try {
            return getConstant("sessionToken");
        } catch (SQLException e) {
            throw new DatabaseException("Failed to get session token from database", e);
        }
    }
    public void setSessionToken(String token) throws DatabaseException {
        try {
            setConstant("sessionToken", token);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to store session token in database", e);
        }
    }

    public LocalDateTime getLastSyncTime() throws DatabaseException {
        try {
            var value = getConstant("lastSyncTime");
            return value != null ? LocalDateTime.parse(value) : null;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to get last sync time from database", e);
        }
    }
    public void setLastSyncTime(LocalDateTime time) throws DatabaseException {
        try {
            setConstant("lastSyncTime", time.toString());
        } catch (SQLException e) {
            throw new DatabaseException("Failed to store last sync time in database", e);
        }
    }
}
