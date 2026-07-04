package dev.ecasept.unitodo.client.db;

import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.db.querybuilder.TransactionFunction;
import dev.ecasept.unitodo.shared.db.querybuilder.batch.Batcher;
import dev.ecasept.unitodo.shared.db.querybuilder.SortOrder;
import dev.ecasept.unitodo.shared.db.querybuilder.QueryBuilder;
import dev.ecasept.unitodo.shared.models.db.ClientTask;
import dev.ecasept.unitodo.shared.models.db.TaskState;
import dev.ecasept.unitodo.shared.utils.DateFormat;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ClientDatabaseRepository {

    private final QueryBuilder db;

    public ClientDatabaseRepository(QueryBuilder db) {
        this.db = db;
    }

    public Optional<ClientTask> getTask(String uuid) throws DatabaseException {
        try (var query = db
                .select()
                .from("tasks")
                .filter(it -> it.eq("uuid", uuid))
                .prepare()) {
            return query.executeSingle(ClientTask::fromResultSet);
        } catch (SQLException | DatabaseException e) {
            throw new DatabaseException("Failed to get task from database", e);
        }
    }

    public ArrayList<ClientTask> getTasks(List<UUID> uuids) throws DatabaseException {
        try (var query = db
                .select()
                .from("tasks")
                .filter(it -> it.eqAny("uuid", uuids))
                .prepare()) {
            return query.executeMulti(ClientTask::fromResultSet);
        } catch (SQLException | DatabaseException e) {
            throw new DatabaseException("Failed to get tasks from database", e);
        }
    }

    public ArrayList<ClientTask> getTasks(TaskState state, SortOrder order, boolean includeDeleted) throws DatabaseException {
        try (var query = db
                .select()
                .from("tasks")
                .filter(it -> it.eq("state", state.toInt()))
                .orderBy(order)
                .when(!includeDeleted, it -> it.filter(i -> i.eq("isDeleted", false)))
                .prepare()) {
            return query.executeMulti(ClientTask::fromResultSet);
        } catch (SQLException | DatabaseException e) {
            throw new DatabaseException("Failed to get tasks from database", e);
        }
    }

    public void upsertTask(ClientTask task) throws DatabaseException {
        upsertTasks(List.of(task));
    }

    public void upsertTasks(List<ClientTask> tasks) throws DatabaseException {
        var batcher = new Batcher();
        var queryBuilder = db
                .insert()
                .v("uuid", batcher.placeholder())
                .v("title", batcher.placeholder())
                .v("description", batcher.placeholder())
                .v("state", batcher.placeholder())
                .v("priority", batcher.placeholder())
                .v("dueDate", batcher.placeholder())
                .v("dueTime", batcher.placeholder())
                .v("titleChanged", batcher.placeholder())
                .v("descriptionChanged", batcher.placeholder())
                .v("stateChanged", batcher.placeholder())
                .v("priorityChanged", batcher.placeholder())
                .v("dueDateChanged", batcher.placeholder())
                .v("dueTimeChanged", batcher.placeholder())
                .v("completedAt", batcher.placeholder())
                .v("isDeleted", batcher.placeholder())
                .v("deletedChanged", batcher.placeholder())
                .into("tasks")
                .onConflict("uuid")
                .doUpdate(it -> it.copy(
                        "title", "description", "state", "priority", "dueDate", "dueTime", "titleChanged", "descriptionChanged", "stateChanged", "priorityChanged", "dueDateChanged", "dueTimeChanged", "completedAt", "isDeleted", "deletedChanged"
                ));


        try (var query = queryBuilder.prepare()) {
            for (var task : tasks) {
                batcher.fill(
                        task.uuid(),
                        task.title().get(),
                        task.description().get(),
                        task.state().get().toInt(),
                        task.priority().get().toInt(),
                        DateFormat.toLong(task.dueDate().get()),
                        task.dueTime().get().map(DateFormat::toLong).orElse(null),
                        DateFormat.toLong(task.title().getLastUpdated()),
                        DateFormat.toLong(task.description().getLastUpdated()),
                        DateFormat.toLong(task.state().getLastUpdated()),
                        DateFormat.toLong(task.priority().getLastUpdated()),
                        DateFormat.toLong(task.dueDate().getLastUpdated()),
                        DateFormat.toLong(task.dueTime().getLastUpdated()),
                        task.state().get().getCompletedAt().map(DateFormat::toLong).orElse(null),
                        task.isDeleted().get(),
                        DateFormat.toLong(task.isDeleted().getLastUpdated())
                );
                query.execute();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to store new task", e);
        }
    }
    public void deleteTask(UUID uuid) throws DatabaseException {
        deleteTasks(List.of(uuid));
    }

    public void deleteTasks(List<UUID> uuids) throws DatabaseException {
        try (var query = db
                .delete()
                .from("tasks")
                .filter(it -> it.eqAny("uuid", uuids))
                .prepare()) {
            query.execute();
        } catch (SQLException | DatabaseException e) {
            throw new DatabaseException("Failed to delete tasks from database", e);
        }
    }

    public ArrayList<ClientTask> getAllTasks() throws DatabaseException {
        try (var query = db
                .select()
                .from("tasks")
                .prepare()) {
            return query.executeMulti(ClientTask::fromResultSet);
        } catch (SQLException | DatabaseException e) {
            throw new DatabaseException("Failed to get all tasks from database", e);
        }
    }

    public ArrayList<ClientTask> getTasksModifiedSince(LocalDateTime lastSyncTime) throws DatabaseException {
        long lastSync = DateFormat.toLong(lastSyncTime);
        try (var query = db
                .select()
                .from("tasks")
                .filter(it -> it
                        .defaultOr(c -> c
                                .ge("titleChanged", lastSync)
                                .ge("descriptionChanged", lastSync)
                                .ge("stateChanged", lastSync)
                                .ge("priorityChanged", lastSync)
                                .ge("dueDateChanged", lastSync)
                                .ge("dueTimeChanged", lastSync)
                                .ge("deletedChanged", lastSync)
                        )
                )
                .prepare()) {
            return query.executeMulti(ClientTask::fromResultSet);
        } catch (SQLException | DatabaseException e) {
            throw new DatabaseException("Failed to get modified tasks from database", e);
        }
    }

    public ArrayList<ClientTask> searchTasks(String query) throws DatabaseException {
        try (var q = db
                .select()
                .from("tasks")
                .filter(it -> it
                        .eq("isDeleted", false)
                        .or(
                                c -> c.contains("title", query),
                                c -> c.contains("description", query)
                        )
                )
                .prepare()) {
            return q.executeMulti(ClientTask::fromResultSet);
        } catch (SQLException | DatabaseException e) {
            throw new DatabaseException("Failed to search tasks in database", e);
        }
    }



    public <T> T transaction(TransactionFunction<T> function) throws DatabaseException, SQLException {
        return db.transaction(function);
    }




    private Optional<String> getVariable(String key) throws DatabaseException, SQLException {
        try (var query = db
                .select("value")
                .from("variables")
                .filter(it -> it.eq("key", key))
                .prepare()) {
            return query.executeSingle(it -> it.getString("value"));
        }
    }
    private void setVariable(String key, String value) throws DatabaseException, SQLException {
        try (var query = db
                .insert()
                .v("key", key)
                .v("value", value)
                .into("variables")
                .onConflict("key")
                .doUpdate(it -> it.copy("value"))
                .prepare()) {
            query.execute();
        }
    }
    private void deleteVariable(String key) throws DatabaseException, SQLException {
        try (var query = db
                .delete()
                .from("variables")
                .filter(it -> it.eq("key", key))
                .prepare()) {
            query.execute();
        }
    }

    public Optional<String> getSessionToken() throws DatabaseException {
        try {
            return getVariable("sessionToken");
        } catch (SQLException | DatabaseException e) {
            throw new DatabaseException("Failed to get session token from database", e);
        }
    }
    public void setSessionToken(String token) throws DatabaseException {
        try {
            setVariable("sessionToken", token);
        } catch (SQLException | DatabaseException e) {
            throw new DatabaseException("Failed to store session token in database", e);
        }
    }
    public void deleteSessionToken() throws DatabaseException {
        try {
            deleteVariable("sessionToken");
        } catch (SQLException | DatabaseException e) {
            throw new DatabaseException("Failed to delete session token from database", e);
        }
    }

    public Optional<LocalDateTime> getLastSyncTime() throws DatabaseException {
        try {
            var value = getVariable("lastSyncTime");
            return value.map(DateFormat::fromString);
        } catch (SQLException | DatabaseException e) {
            throw new DatabaseException("Failed to get last sync time from database", e);
        }
    }
    public void setLastSyncTime(LocalDateTime time) throws DatabaseException {
        try {
            setVariable("lastSyncTime", DateFormat.toString(time));
        } catch (SQLException | DatabaseException e) {
            throw new DatabaseException("Failed to store last sync time in database", e);
        }
    }
}
