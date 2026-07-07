package dev.ecasept.unitodo.client;

import dev.ecasept.unitodo.client.api.ApiClient;
import dev.ecasept.unitodo.client.api.exception.ApiException;
import dev.ecasept.unitodo.client.db.ClientDatabaseRepository;
import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.db.querybuilder.SortOrder;
import dev.ecasept.unitodo.shared.models.api.Password;
import dev.ecasept.unitodo.shared.models.db.ClientTask;
import dev.ecasept.unitodo.shared.models.db.TaskState;
import dev.ecasept.unitodo.shared.utils.Log;
import sync.SyncService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

public class DataManager {
    private static final String TAG = "DataManager";
    private static final LocalDateTime LOWEST_SYNC_TIME = LocalDateTime.of(1970, 1, 1, 0, 0);
    private final ClientDatabaseRepository db;
    private final ApiClient apiClient;
    private final SyncService syncService;
    private final ConcurrentMap<UUID, ClientTask> unsynced = new ConcurrentHashMap<>();
    private LocalDateTime cachedLastSyncTime = null;
    private final ExecutorService syncExecutor = Executors.newSingleThreadExecutor();


    public boolean isLoggedIn() throws DatabaseException {
        return db.getSessionToken().isPresent();
    }

    private Optional<LocalDateTime> getLastSyncTime() throws DatabaseException {
        if (cachedLastSyncTime == null) {
            var time = db.getLastSyncTime();
            if (time.isEmpty()) {
                return time;
            }
            cachedLastSyncTime = time.get();
        }
        return Optional.of(cachedLastSyncTime);
    }
    private void setLastSyncTime(LocalDateTime time) throws DatabaseException {
        db.setLastSyncTime(time);
        cachedLastSyncTime = time;
    }

    public DataManager(ClientDatabaseRepository db, ApiClient apiClient, SyncService syncService) {
        this.db = db;
        this.apiClient = apiClient;
        this.syncService = syncService;
    }

    public CompletableFuture<Void> login(String username, Password password, boolean discardLocalChanges) {
        return CompletableFuture.runAsync(() -> {
            try {
                var sessionToken = apiClient.login(username, password);
                db.setSessionToken(sessionToken);
                apiClient.setSessionToken(sessionToken);
                if (discardLocalChanges) {
                    db.deleteAllTasks();
                    unsynced.clear();
                }
            } catch (ApiException | DatabaseException e) {
                Log.e(TAG, "Login failed", e);
                throw new CompletionException(e);
            } catch (Throwable t) {
                Log.e(TAG, "Unexpected error during login", t);
                throw t;
            }
        }, syncExecutor).thenCompose(v -> sync(true).thenApply(changed -> null));
    }
    public CompletableFuture<Void> register(String username, Password password) {
        return CompletableFuture.runAsync(() -> {
            try {
                var sessionToken = apiClient.register(username, password);
                db.setSessionToken(sessionToken);
                apiClient.setSessionToken(sessionToken);
            } catch (ApiException | DatabaseException e) {
                Log.e(TAG, "Registration failed", e);
                throw new CompletionException(e);
            } catch (Throwable t) {
                Log.e(TAG, "Unexpected error during registration", t);
                throw t;
            }
        }, syncExecutor).thenCompose(v -> sync(true).thenApply(changed -> null));
    }
    public void logout() throws DatabaseException {
        try {
            db.deleteSessionToken();
            apiClient.setSessionToken(null);
        } catch (DatabaseException e) {
            Log.e(TAG, "Logout failed", e);
            throw e;
        }
    }
    public CompletableFuture<Void> deleteAccount(Password password) {
        return CompletableFuture.runAsync(() -> {
            try {
                apiClient.deleteAccount(password);
                logout();
            } catch (ApiException | DatabaseException e) {
                Log.e(TAG, "Account deletion failed", e);
                throw new CompletionException(e);
            } catch (Throwable t) {
                Log.e(TAG, "Unexpected error during account deletion", t);
                throw t;
            }
        }, syncExecutor);
    }

    /** Forcibly synchronizes the whole client state with the server */
    public CompletableFuture<Boolean> synchronize() throws DatabaseException {
        if (!isLoggedIn()) {
            return CompletableFuture.completedFuture(false);
        }
        return sync(true);
    }

    private CompletableFuture<Boolean> sync() {
        return sync(false);
    }

    private CompletableFuture<Boolean> sync(boolean full) {
        Log.i(TAG, "Starting synchronization of " + unsynced.size() + " tasks");
        var syncStart = LocalDateTime.now();

        if (full) {
            try {
                unsynced.clear();
                var allTasks = db.getAllTasks();
                for (var task : allTasks) {
                    unsynced.put(task.uuid(), task);
                }
            } catch (DatabaseException e) {
                return CompletableFuture.failedFuture(new CompletionException(e));
            }
        }

        var unsyncedSnapshot = Map.copyOf(unsynced);
        unsynced.clear();

         Runnable restoreSync = () -> {
            Log.i(TAG, "Restoring " + unsyncedSnapshot.size() + " unsynced tasks to queue");
            unsyncedSnapshot.forEach(unsynced::putIfAbsent);
        };

         return CompletableFuture.supplyAsync(() -> {
            try {
                var lastSyncTime = full ? Optional.of(LOWEST_SYNC_TIME) : getLastSyncTime();
                try {
                    boolean changed = syncService.synchronize(unsyncedSnapshot.values().toArray(new ClientTask[0]), lastSyncTime, syncStart);
                    setLastSyncTime(syncStart);

                    // Delete tombstones from the database after successful sync
                    boolean deleted = db.deleteTombstonesOlderThan(syncStart);

                    return changed || deleted;

                } catch (ApiException e) {
                    Log.w(TAG, "Failed to synchronize tasks, will retry on next sync", e);
                    restoreSync.run();
                    return false;
                }
            } catch (DatabaseException e) {
                Log.e(TAG, "Failed to access database during synchronization", e);
                restoreSync.run();
                throw new CompletionException(e);
            } catch (Throwable t) {
                Log.e(TAG, "Unexpected error during synchronization", t);
                restoreSync.run();
                throw t;
            }
        }, syncExecutor);
    }

    /**
     * Updates the specified task or creates it if it doesn't exist yet. Tries to update the server as well.
     * <p>
     * Tasks are stored based on their UUID, so if no task with the contained UUID exists, it will be created,
     * otherwise the task with the matching UUID will have its fields updated to reflect those of the provided task.
     * <p>
     * If the server cannot be notified of the change
     *
     * @param task The task that should be created or updated
     * @throws DatabaseException If any database access fails
     */
    public CompletableFuture<Boolean> upsertTask(ClientTask task) throws DatabaseException {
        db.upsertTask(task);
        unsynced.put(task.uuid(), task);
        if (isLoggedIn()) {
            return sync();
        } else {
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * Deletes the specified task. Tries to update the server as well.
     *
     * @param task The task that should be deleted
     * @throws DatabaseException If any database access fails
     */
    public CompletableFuture<Boolean> deleteTask(ClientTask task) throws DatabaseException {
        return upsertTask(task.withDeleted(true));
    }


    public CompletableFuture<Void> initialize() throws DatabaseException {
        // Check for unsynced tasks in the database
        var lastSyncTime = getLastSyncTime();
        if (lastSyncTime.isEmpty()) {
            Log.i(TAG, "No last sync time found, syncing all tasks");
            var allTasks = db.getAllTasks();
            Log.i(TAG, "Found " + allTasks.size() + " unsynced tasks in the database, will synchronize on next sync");
            for (var task : allTasks) {
                unsynced.put(task.uuid(), task);
            }
        } else {
            var modifiedTasks = db.getTasksModifiedSince(lastSyncTime.get());
            if (!modifiedTasks.isEmpty()) {
                Log.i(TAG, "Found " + modifiedTasks.size() + " unsynced tasks in the database, will synchronize on next sync");
                for (var task : modifiedTasks) {
                    unsynced.put(task.uuid(), task);
                }
            }
        }
        // Cache session token
        var sessionToken = db.getSessionToken();
        apiClient.setSessionToken(sessionToken.orElse(null));
        if (sessionToken.isPresent()) {
            // Sync with server to get any changes that might have happened while the client was offline
            return sync().thenApply(changed -> null);
        }
        return CompletableFuture.completedFuture(null);
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


    public void close() {
        syncExecutor.shutdown();
    }
}
