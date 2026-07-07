package sync;

import dev.ecasept.unitodo.client.api.ApiClient;
import dev.ecasept.unitodo.client.api.exception.ApiException;
import dev.ecasept.unitodo.client.db.ClientDatabaseRepository;
import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.models.api.SyncRequest;
import dev.ecasept.unitodo.shared.models.db.ClientTask;
import dev.ecasept.unitodo.shared.sync.Synchronizer;
import dev.ecasept.unitodo.shared.utils.Log;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SyncService {
    private final ClientDatabaseRepository db;
    private final Synchronizer synchronizer;
    private final ApiClient apiClient;
    public SyncService(ClientDatabaseRepository db, Synchronizer synchronizer, ApiClient apiClient) {
        this.db = db;
        this.synchronizer = synchronizer;
        this.apiClient = apiClient;
    }


    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public boolean synchronize(ClientTask[] modifiedTasks, Optional<LocalDateTime> lastSyncTime, LocalDateTime syncStart) throws ApiException, DatabaseException {
        var syncRequest = new SyncRequest(modifiedTasks, lastSyncTime);
        var res = apiClient.sync(syncRequest);

        try {
            var changed = db.transaction(
                    () -> {
                        var clientTaskList = db.getTasks(Arrays.stream(res.tasks()).map(ClientTask::uuid).toList());
                        var clientTasks = clientTaskList.stream().collect(Collectors.toUnmodifiableMap(ClientTask::uuid, Function.identity()));
                        var serverTasks = Arrays.stream(res.tasks()).collect(Collectors.toUnmodifiableMap(ClientTask::uuid, Function.identity()));

                        var newTasks = synchronizer.synchronizeClient(clientTasks, serverTasks);
                        Log.i("Synchronizer", "Merged tasks: " + newTasks.size());

                        db.upsertTasksWithOlderFields(newTasks);

                        boolean deletedAnything = false;
                        if (res.presentList().isPresent()) {
                            deletedAnything = db.deleteTasksNotPresentAndOlderThan(Arrays.asList(res.presentList().get()), syncStart);
                        }

                        return !newTasks.isEmpty() || deletedAnything;
                    }
            );
            return changed;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to synchronize tasks", e);
        }
    }
}
