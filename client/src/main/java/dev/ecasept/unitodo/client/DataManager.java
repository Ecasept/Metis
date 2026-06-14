package dev.ecasept.unitodo.client;

import dev.ecasept.unitodo.client.api.ApiClient;
import dev.ecasept.unitodo.client.api.exception.ApiException;
import dev.ecasept.unitodo.client.db.ClientDatabaseRepository;
import dev.ecasept.unitodo.client.sync.SyncState;
import dev.ecasept.unitodo.client.sync.Synchronizer;
import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.models.db.ClientTask;
import dev.ecasept.unitodo.shared.models.db.TimestampedField;
import dev.ecasept.unitodo.shared.utils.Log;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

public class DataManager {
    private static final String TAG = "DataManager";
    private final ClientDatabaseRepository db;
    private final ApiClient apiClient;
    private final Synchronizer synchronizer;
    private final HashMap<UUID, ClientTask> unsynced = new HashMap<>();
    private SyncState state = SyncState.NeedsFullSync;
    private LocalDateTime cachedLastSyncTime = null;

    private LocalDateTime getLastSyncTime() throws DatabaseException {
        if (cachedLastSyncTime == null) {
            cachedLastSyncTime = db.getLastSyncTime().orElse(LocalDateTime.MIN);
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
        apiClient.setSessionToken(sessionToken);
        db.setSessionToken(sessionToken);
    }
    public void register(String username, String password) throws ApiException, DatabaseException {
        var sessionToken = apiClient.register(username, password);
        apiClient.setSessionToken(sessionToken);
        db.setSessionToken(sessionToken);
    }
    public void logout() throws DatabaseException {
        apiClient.setSessionToken(null);
        db.deleteSessionToken();
    }
    public void deleteAccount(String password) throws ApiException, DatabaseException {
        apiClient.deleteAccount(password);
        apiClient.setSessionToken(null);
        db.deleteSessionToken();
    }

    /**
     * Synchronizes all tasks that have been modified since the last synchronization time, and updates the last synchronization time to now.
     * Sidesteps the unsynced cache, so make sure to always update the db even if you also update the unsynced cache.
     * @throws DatabaseException If any database access fails
     */
    private void synchronizeAll() throws DatabaseException {
        var lastSyncTime = getLastSyncTime();
        var tasks = db.getTasksModifiedSince(lastSyncTime);
        try {
            synchronizer.synchronize(tasks, lastSyncTime);
            setLastSyncTime(LocalDateTime.now());
            state = SyncState.Synced;
        } catch (ApiException e) {
            Log.w(TAG, "Failed to synchronize tasks, will retry on next sync", e);
            state = SyncState.NeedsFullSync;
        }
    }

    /**
     * Only syncs the unsynced task cache, avoiding a database query to fetch all modified tasks.
     * Should only be used when the unsynced cache is guaranteed to contain all modified tasks, i.e. when {@link DataManager#state} is {@link SyncState#Synced} or {@link SyncState#Dirty}.
     * @throws DatabaseException If any database access fails
     */
    private void syncUnsynced() throws DatabaseException {
        var lastSyncTime = db.getLastSyncTime().orElse(LocalDateTime.MIN);
        try {
            synchronizer.synchronize(unsynced.values().toArray(new ClientTask[0]), lastSyncTime);
            unsynced.clear();
            state = SyncState.Synced;
            setLastSyncTime(LocalDateTime.now());
        } catch (ApiException e) {
            Log.w(TAG, "Failed to synchronize tasks, will retry on next sync", e);
            state = SyncState.Dirty;
        }
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
        switch (state) {
            case Synced, Dirty -> {
                // Only sync this task and others in the unsynced cache
                unsynced.put(task.uuid(), task);
                syncUnsynced();
            }
            case NeedsFullSync -> {
                // There might be some changes in the db (e.g. from a previous session) that need to be synchronized, so do a full sync
                synchronizeAll();
            }
        }
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
}
