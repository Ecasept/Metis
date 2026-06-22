package dev.ecasept.unitodo.client.sync;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import dev.ecasept.unitodo.client.api.ApiClient;
import dev.ecasept.unitodo.client.api.exception.ApiException;
import dev.ecasept.unitodo.client.db.ClientDatabaseRepository;
import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.models.api.SyncRequest;
import dev.ecasept.unitodo.shared.models.db.ClientTask;
import dev.ecasept.unitodo.shared.models.db.ServerTask;
import dev.ecasept.unitodo.shared.models.db.TimestampedField;

public class Synchronizer {
    private final ClientDatabaseRepository db;
    private final ApiClient apiClient;
    public Synchronizer(ClientDatabaseRepository db, ApiClient apiClient) {
        this.db = db;
        this.apiClient = apiClient;
    }


    public void synchronize(ClientTask[] modifiedTasks, LocalDateTime lastSyncTime) throws ApiException, DatabaseException {
        var syncRequest = new SyncRequest(modifiedTasks, lastSyncTime);
        var res = apiClient.sync(syncRequest);

        try {
            db.transaction(
                    () -> {
                        var clientTaskList = db.getTasks(Arrays.stream(res.tasks()).map(ClientTask::uuid).toList());
                        var clientTasks = clientTaskList.stream().collect(Collectors.toUnmodifiableMap(ClientTask::uuid, Function.identity()));
                        var serverTasks = Arrays.stream(res.tasks()).collect(Collectors.toUnmodifiableMap(ClientTask::uuid, Function.identity()));

                        var newTasks = mergeTasks(clientTasks, serverTasks);

                        db.upsertTasks(newTasks);

                        if (res.presentList().isPresent()) {
                            db.deleteTasks(Arrays.asList(res.presentList().get()));
                        }

                        return null;
                    }
            );
        } catch (SQLException e) {
            throw new DatabaseException("Failed to synchronize tasks", e);
        }
    }


    private <V> TimestampedField<V> pickNewer(TimestampedField<V> a, TimestampedField<V> b) {
        var aDate = a.getLastUpdated();
        var bDate = b.getLastUpdated();
        if (aDate.isAfter(bDate)) {
            return a;
        } else {
            return b;
        }
    }

    private ClientTask merge(ClientTask a, ClientTask b) {
        var title = pickNewer(a.title(), b.title());
        var description = pickNewer(a.description(), b.description());
        var state = pickNewer(a.state(), b.state());
        var priority = pickNewer(a.priority(), b.priority());
        var dueDate = pickNewer(a.dueDate(), b.dueDate());
        var dueTime = pickNewer(a.dueTime(), b.dueTime());
        var isDeleted = pickNewer(a.isDeleted(), b.isDeleted());
        return new ClientTask(a.uuid(), title, description, state, priority, dueDate, dueTime, isDeleted);
    }

    private List<ClientTask> mergeTasks(Map<UUID, ClientTask> cur, Map<UUID, ClientTask> other) {
        ArrayList<ClientTask> newTasks = new ArrayList<>();

        for (var entry : other.entrySet()) {
            var uuid = entry.getKey();
            var otherTask = entry.getValue();
            var thisTask = cur.get(uuid);

            if (thisTask == null) {
                // Task was added by server
                newTasks.add(otherTask);
            } else {
                // Task exists on both sides, merge them
                var merged = merge(thisTask, otherTask);
                newTasks.add(merged);
            }
        }
        return newTasks;
    }
}
