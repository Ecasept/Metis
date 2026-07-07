package dev.ecasept.unitodo.client.db;

import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.db.querybuilder.expressions.conditions.C;
import dev.ecasept.unitodo.shared.utils.ThrowingSupplier2;
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
                .filter(t -> C.eq("uuid", uuid))
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
                .filter(t -> C.eqAny("uuid", uuids))
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
                .filter(t -> C.eq("state", state.toInt()))
                .orderBy(order)
                .when(!includeDeleted, it -> it.filter(t -> C.eq("isDeleted", false)))
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
                .doUpdate((cr, t) -> cr.copy(
                        "title", "description", "state", "priority", "dueDate", "dueTime", "titleChanged", "descriptionChanged", "stateChanged", "priorityChanged", "dueDateChanged", "dueTimeChanged", "completedAt", "isDeleted", "deletedChanged"
                ));


        try (var query = queryBuilder.prepare()) {
            for (var task : tasks) {
                batcher.fill(
                        task.uuid(),
                        task.getTitle(),
                        task.getDescription(),
                        task.getState().toInt(),
                        task.getPriority().toInt(),
                        DateFormat.toLong(task.getDueDate()),
                        task.getDueTime().map(DateFormat::toLong).orElse(null),
                        DateFormat.toLong(task.title().getLastUpdated()),
                        DateFormat.toLong(task.description().getLastUpdated()),
                        DateFormat.toLong(task.state().getLastUpdated()),
                        DateFormat.toLong(task.priority().getLastUpdated()),
                        DateFormat.toLong(task.dueDate().getLastUpdated()),
                        DateFormat.toLong(task.dueTime().getLastUpdated()),
                        task.getState().getCompletedAt().map(DateFormat::toLong).orElse(null),
                        task.getDeleted(),
                        DateFormat.toLong(task.isDeleted().getLastUpdated())
                );
                query.execute();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to store new task", e);
        }
    }

    /** Creates a new task or updates the fields of an existing task that were changed before the field of the provided task */
    public void upsertTasksWithOlderFields(List<ClientTask> tasks) throws DatabaseException {
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
                .doUpdate((cr, t) ->
                        cr.setIfNewer(t, "title", "titleChanged")
                                .setIfNewer(t, "description", "descriptionChanged")
                                .setIfNewer(t, "state", "stateChanged")
                                .setIfNewer(t, "priority", "priorityChanged")
                                .setIfNewer(t, "dueDate", "dueDateChanged")
                                .setIfNewer(t, "dueTime", "dueTimeChanged")
                                .setIfNewer(t, "completedAt", "stateChanged")
                                .setIfNewer(t, "isDeleted", "deletedChanged")
                );

        try (var query = queryBuilder.prepare()) {
            for (var task : tasks) {
                batcher.fill(
                        task.uuid(),
                        task.getTitle(),
                        task.getDescription(),
                        task.getState().toInt(),
                        task.getPriority().toInt(),
                        DateFormat.toLong(task.getDueDate()),
                        task.getDueTime().map(DateFormat::toLong).orElse(null),
                        DateFormat.toLong(task.title().getLastUpdated()),
                        DateFormat.toLong(task.description().getLastUpdated()),
                        DateFormat.toLong(task.state().getLastUpdated()),
                        DateFormat.toLong(task.priority().getLastUpdated()),
                        DateFormat.toLong(task.dueDate().getLastUpdated()),
                        DateFormat.toLong(task.dueTime().getLastUpdated()),
                        task.getState().getCompletedAt().map(DateFormat::toLong).orElse(null),
                        task.getDeleted(),
                        DateFormat.toLong(task.isDeleted().getLastUpdated())
                );
                query.execute();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to store new task", e);
        }
    }

    /** Deletes all tasks that are not present in the given list of UUIDs and have not been modified since the given timestamp. */
    public boolean deleteTasksNotPresentAndOlderThan(List<UUID> uuids, LocalDateTime before) throws DatabaseException {
        try (var query = db
                .delete()
                .from("tasks")
                .filter(t ->
                        C.not(C.eqAny("uuid", uuids))
                        .lt("titleChanged", before)
                        .lt("descriptionChanged", before)
                        .lt("stateChanged", before)
                        .lt("priorityChanged", before)
                        .lt("dueDateChanged", before)
                        .lt("dueTimeChanged", before)
                        .lt("deletedChanged", before)
                ).prepare()) {
            int rows = query.execute();
            return rows > 0;
        } catch (SQLException | DatabaseException e) {
            throw new DatabaseException("Failed to delete tasks from database", e);
        }
    }

    /** Deletes all tasks that are marked as deleted and have not been modified since the given timestamp. */
    public boolean deleteTombstonesOlderThan(LocalDateTime before) throws DatabaseException {
        try (var query = db
                .delete()
                .from("tasks")
                .filter(t -> C.eq("isDeleted", true)
                        .lt("deletedChanged", before)
                ).prepare()) {
            int rows = query.execute();
            return rows > 0;
        } catch (SQLException | DatabaseException e) {
            throw new DatabaseException("Failed to delete tombstones from database", e);
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
                .filter(t -> C.or(
                        C.ge("titleChanged", lastSync),
                        C.ge("descriptionChanged", lastSync),
                        C.ge("stateChanged", lastSync),
                        C.ge("priorityChanged", lastSync),
                        C.ge("dueDateChanged", lastSync),
                        C.ge("dueTimeChanged", lastSync),
                        C.ge("deletedChanged", lastSync)
                ))
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
                .filter(cols ->
                    C.and(
                        C.eq("isDeleted", false),
                        C.or(
                            C.contains("title", query),
                            C.contains("description", query)
                        )
                    )
                )
                .prepare()) {
            return q.executeMulti(ClientTask::fromResultSet);
        } catch (SQLException | DatabaseException e) {
            throw new DatabaseException("Failed to search tasks in database", e);
        }
    }



    public <T> T transaction(ThrowingSupplier2<T, DatabaseException, SQLException> function) throws DatabaseException, SQLException {
        return db.transaction(function);
    }




    private Optional<String> getVariable(String key) throws DatabaseException, SQLException {
        try (var query = db
                .select("value")
                .from("variables")
                .filter(t -> C.eq("key", key))
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
                .doUpdate((cr, t) -> cr.copy("value"))
                .prepare()) {
            query.execute();
        }
    }
    private boolean deleteVariable(String key) throws DatabaseException, SQLException {
        try (var query = db
                .delete()
                .from("variables")
                .filter(t -> C.eq("key", key))
                .prepare()) {
            int rows = query.execute();
            return rows > 0;
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
