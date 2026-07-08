package dev.ecasept.unitodo.server.db;

import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.db.querybuilder.expressions.conditions.C;
import dev.ecasept.unitodo.shared.utils.ThrowingBiSupplier;
import dev.ecasept.unitodo.shared.db.querybuilder.batch.Batcher;
import dev.ecasept.unitodo.shared.db.querybuilder.QueryBuilder;
import dev.ecasept.unitodo.shared.db.querybuilder.SortOrder;
import dev.ecasept.unitodo.shared.models.db.ServerTask;
import dev.ecasept.unitodo.shared.models.db.TaskState;
import dev.ecasept.unitodo.shared.utils.DateFormat;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ServerDatabaseRepository {
    private final QueryBuilder db;

    public ServerDatabaseRepository(QueryBuilder db) {
        this.db = db;
    }


    /** Returns a list of tasks with the specified uuids belonging to the specified user */
    public ArrayList<ServerTask> getTasks(List<UUID> uuids, UUID userId) throws DatabaseException {
        try (var query = db
                .select()
                .from("tasks")
                .filter(t ->
                        C.eqAny("uuid", uuids).eq("userId", userId)
                )
                .prepare()) {
            return query.executeMulti(ServerTask::fromResultSet);
        } catch (SQLException | DatabaseException e) {
            throw new DatabaseException("Failed to get tasks from database", e);
        }
    }

    /** Returns all tasks belonging to the specified user */
    public ServerTask[] getAllTasks(UUID userId) throws DatabaseException {
        try (var query = db
                .select()
                .from("tasks")
                .filter(t -> C.eq("userId", userId))
                .prepare()) {
            return query.executeMulti(ServerTask::fromResultSet).toArray(new ServerTask[0]);
        } catch (SQLException | DatabaseException e) {
            throw new DatabaseException("Failed to get all tasks from database", e);
        }
    }

    /** Inserts, or, if already present, updates the provided tasks into the database */
    public void upsertTasks(List<ServerTask> tasks) throws DatabaseException {
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
                .v("userId", batcher.placeholder())
                .into("tasks")
                .onConflict("uuid", "userId")
                .doUpdate((cr, t) -> cr.copy(
                        "title", "description", "state", "priority", "dueDate", "dueTime", "titleChanged", "descriptionChanged", "stateChanged", "priorityChanged", "dueDateChanged", "dueTimeChanged", "completedAt", "isDeleted", "deletedChanged", "userId"
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
                        DateFormat.toLong(task.isDeleted().getLastUpdated()),
                        task.userId()
                );
                query.execute();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to store new task", e);
        }
    }

    /** Creates a new user with the specified username and password hash and returns their new uuid */
    public UUID createUser(String username, String passwordHash) throws DatabaseException {
        var uuid = UUID.randomUUID();
        try (var query = db.insert()
                .v("username", username)
                .v("passwordHash", passwordHash)
                .v("uuid", uuid)
                .into("users")
                .prepare()) {
            query.execute();
            return uuid;
        } catch (SQLException | DatabaseException e) {
            throw new DatabaseException("Failed to create user in database", e);
        }
    }

    /** Deletes the user with the specified id and returns if such a user existed */
    public boolean deleteUser(UUID userId) throws DatabaseException {
        try (var query = db.delete()
                .from("users")
                .filter(t -> C.eq("uuid", userId))
                .prepare()) {
            int rows = query.execute();
            return rows > 0;
        } catch (SQLException | DatabaseException e) {
            throw new DatabaseException("Failed to delete user from database", e);
        }
    }

    /** Tries to find and return the user with the specified id */
    public Optional<User> getUser(UUID userId) throws DatabaseException {
        try (var query = db
                .select()
                .from("users")
                .filter(t -> C.eq("uuid", userId))
                .prepare()) {
            return query.executeSingle(User::fromResultSet);
        } catch (SQLException | DatabaseException e) {
            throw new DatabaseException("Failed to get user from database", e);
        }
    }

    /** Tries to find and return the user with the specified username */
    public Optional<User> getUserByUsername(String username) throws DatabaseException {
        try (var query = db
                .select()
                .from("users")
                .filter(t -> C.eq("username", username))
                .prepare()) {
            return query.executeSingle(User::fromResultSet);
        } catch (SQLException | DatabaseException e) {
            throw new DatabaseException("Failed to get user from database", e);
        }
    }

    /** Wrapper for {@link QueryBuilder#transaction(ThrowingBiSupplier)} */
    public <T> T transaction(ThrowingBiSupplier<T, DatabaseException, SQLException> function) throws DatabaseException, SQLException {
        return db.transaction(function);
    }


    /** Returns a list of uuids of all the tasks the specified user id has associated with them */
    public List<UUID> getAllTaskUUIDs(UUID userId) throws DatabaseException {
        try (var query = db
                .select("uuid")
                .from("tasks")
                .filter(t -> C.eq("userId", userId))
                .prepare()
        ) {
            return query.executeMulti(rs -> UUID.fromString(rs.getString("uuid")));
        } catch (SQLException | DatabaseException e) {
            throw new DatabaseException("Failed to get all task UUIDs from database", e);
        }
    }

    /** Returns all tasks belonging to the specified user that have been modified since the specified timestamp */
    public ServerTask[] getTasksModifiedSince(LocalDateTime lastSyncTime, UUID userId) throws DatabaseException {
        long lastSync = DateFormat.toLong(lastSyncTime);
        try (var query = db
            .select()
            .from("tasks")
            .filter(cols ->
                C.and(
                    C.eq(cols.col("userId"), userId),
                    C.or(
                        C.ge("titleChanged", lastSync),
                        C.ge("descriptionChanged", lastSync),
                        C.ge("stateChanged", lastSync),
                        C.ge("priorityChanged", lastSync),
                        C.ge("dueDateChanged", lastSync),
                        C.ge("dueTimeChanged", lastSync),
                        C.ge("deletedChanged", lastSync)
                    )
                )
            )
            .prepare()) {
            return query.executeMulti(ServerTask::fromResultSet).toArray(new ServerTask[0]);
        } catch (SQLException | DatabaseException e) {
            throw new DatabaseException("Failed to get modified tasks from database", e);
        }
    }
}
