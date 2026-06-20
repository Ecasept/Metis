package dev.ecasept.unitodo.client;

import dev.ecasept.unitodo.client.api.ApiClient;
import dev.ecasept.unitodo.client.api.exception.ApiException;
import dev.ecasept.unitodo.client.db.ClientDatabaseRepository;
import dev.ecasept.unitodo.client.sync.Synchronizer;
import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.db.querybuilder.SortOrder;
import dev.ecasept.unitodo.shared.models.db.ClientTask;
import dev.ecasept.unitodo.shared.models.db.TaskState;
import dev.ecasept.unitodo.shared.models.db.TimestampedField;
import dev.ecasept.unitodo.shared.utils.Log;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class DataManager {
    private static final String TAG = "DataManager";
    private final ClientDatabaseRepository db;
    private final ApiClient apiClient;
    private final Synchronizer synchronizer;
    private final HashMap<UUID, ClientTask> unsynced = new HashMap<>();
    private LocalDateTime cachedLastSyncTime = null;
    private final ExecutorService syncExecutor = Executors.newSingleThreadExecutor();


    public boolean isLoggedIn() throws DatabaseException {
        return db.getSessionToken().isPresent();
    }

    private LocalDateTime getLastSyncTime() throws DatabaseException {
        if (cachedLastSyncTime == null) {
            cachedLastSyncTime = db.getLastSyncTime().orElse(LocalDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC));
        }
        return cachedLastSyncTime;
    }
    private void setLastSyncTime(LocalDateTime time) throws DatabaseException {
        db.setLastSyncTime(time);
        cachedLastSyncTime = time;
    }

    public DataManager(ClientDatabaseRepository db, ApiClient apiClient, Synchronizer synchronizer) {
        this.db = db;
        this.apiClient = apiClient;
        this.synchronizer = synchronizer;
    }

    public void login(String username, String password) throws ApiException, DatabaseException {
        var sessionToken = apiClient.login(username, password);
        db.setSessionToken(sessionToken);
        apiClient.setSessionToken(sessionToken);
    }
    public void register(String username, String password) throws ApiException, DatabaseException {
        var sessionToken = apiClient.register(username, password);
        db.setSessionToken(sessionToken);
        apiClient.setSessionToken(sessionToken);
    }
    public void logout() throws DatabaseException {
        db.deleteSessionToken();
        apiClient.setSessionToken(null);
    }
    public void deleteAccount(String password) throws ApiException, DatabaseException {
        apiClient.deleteAccount(password);
        logout();
    }

    private Consumer<Exception> asyncErrorHandler = e -> {};
    public void setAsyncErrorHandler(Consumer<Exception> asyncErrorHandler) {
        this.asyncErrorHandler = asyncErrorHandler;
    }

    /**
     * @throws DatabaseException If any database access fails
     */
    private void sync() throws DatabaseException {
        var now = LocalDateTime.now();
        syncExecutor.submit(() -> {
            try {
                var lastSyncTime = getLastSyncTime();
                try {
                    synchronizer.synchronize(unsynced.values().toArray(new ClientTask[0]), lastSyncTime);
                    setLastSyncTime(now);
                    unsynced.clear();
                } catch (ApiException e) {
                    Log.w(TAG, "Failed to synchronize tasks, will retry on next sync", e);
                }
            } catch (DatabaseException e) {
                Log.e(TAG, "Failed to access database during synchronization", e);
                asyncErrorHandler.accept(e);
            }
        });
    }

    /**
     * Updates the specified task, or creates it if it doesn't exist yet. Tries to update the server as well.
     * <p>
     * Tasks are stored based on their UUID, so if no task with the contained UUID exist, it will be created,
     * otherwise the task with the matching UUID will have its fields updated to reflect those of the provided task.
     * <p>
     * If the server can not be notified of the change
     *
     * @param task The task that should be created, or the tas
     * @throws DatabaseException If any database access fails
     */
    public void upsertTask(ClientTask task) throws DatabaseException {
        db.upsertTask(task);
        unsynced.put(task.uuid(), task);
        sync();
    }

    /**
     * Deletes the specified task. Tries to update the server as well.
     *
     * @param task The task that should be deleted
     * @throws DatabaseException If any database access fails
     */
    public void deleteTask(ClientTask task) throws DatabaseException {
        var deletedTask = new ClientTask(task.uuid(), task.title(), task.description(), task.state(), task.priority(), task.dueDate(), new TimestampedField<>(true));
        upsertTask(deletedTask);
    }


    public void initialize() throws DatabaseException {
        // Check for unsynced tasks in the database
        var lastSyncTime = getLastSyncTime();
        var modifiedTasks = db.getTasksModifiedSince(lastSyncTime);
        if (!modifiedTasks.isEmpty()) {
            Log.i(TAG, "Found " + modifiedTasks.size() + " unsynced tasks in the database, will synchronize on next sync");
            for (var task : modifiedTasks) {
                unsynced.put(task.uuid(), task);
            }
        }
        // Cache session token
        var sessionToken = db.getSessionToken();
        apiClient.setSessionToken(sessionToken.orElse(null));
        // Sync with server to get any changes that might have happened while the client was offline
        sync();
    }





    public Optional<ClientTask> getTask(String uuid) throws DatabaseException {
        return db.getTask(uuid);
    }

    public ArrayList<ClientTask> getTasks(TaskState state, SortOrder order, boolean includeDeleted) throws DatabaseException {
        return db.getTasks(state, order, includeDeleted);
    }

    public ArrayList<ClientTask> searchTasks(String query) throws DatabaseException {
        return db.searchTasks(query);
    }
}
