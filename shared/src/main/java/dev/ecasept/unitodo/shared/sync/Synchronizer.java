package dev.ecasept.unitodo.shared.sync;

import dev.ecasept.unitodo.shared.models.db.ClientTask;
import dev.ecasept.unitodo.shared.models.db.ServerTask;
import dev.ecasept.unitodo.shared.models.db.Task;
import dev.ecasept.unitodo.shared.models.db.TimestampedField;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

public class Synchronizer {

    private <V> TimestampedField<V> pickNewer(TimestampedField<V> server, TimestampedField<V> client) {
        var serverDate = server.getLastUpdated();
        var clientDate = client.getLastUpdated();
        if (serverDate.isAfter(clientDate)) {
            return server;
        } else {
            if (serverDate.isEqual(clientDate)) {
                // If the timestamps are equal, prefer the server's value
                return server;
            }
            return client;
        }
    }

    private Task<?> resolveDeletion(Task<?> deletedTask, Task<?> existingTask) {
        var maxTimestamp = Stream.of(
                        existingTask.title(), existingTask.description(), existingTask.state(), existingTask.priority(), existingTask.dueDate(), existingTask.dueTime()
                ).map(TimestampedField::getLastUpdated)
                .max(LocalDateTime::compareTo)
                .orElseThrow(() -> new IllegalStateException("No fields to compare for deletion timestamp"));

        if (deletedTask.isDeleted().getLastUpdated().isAfter(maxTimestamp)) {
            return deletedTask;
        } else {
            // Prefer the existing task for equal timestamps
            return existingTask;
        }

    }

    private <T extends Task<T>> T merge(T server, ClientTask client) {
        var title = pickNewer(server.title(), client.title());
        var description = pickNewer(server.description(), client.description());
        var state = pickNewer(server.state(), client.state());
        var priority = pickNewer(server.priority(), client.priority());
        var dueDate = pickNewer(server.dueDate(), client.dueDate());
        var dueTime = pickNewer(server.dueTime(), client.dueTime());

        TimestampedField<Boolean> isDeleted;
        if (server.isDeleted().get() == client.isDeleted().get()) {
            isDeleted = pickNewer(server.isDeleted(), client.isDeleted());
        } else if (server.isDeleted().get()) {
            isDeleted = resolveDeletion(server, client).isDeleted();
        } else {
            isDeleted = resolveDeletion(client, server).isDeleted();
        }
        return server.with(title, description, state, priority, dueDate, dueTime, isDeleted);
    }


    private <T extends Task<T>> List<T> synchronize(Map<UUID, T> cur, Map<UUID, ClientTask> other, Function<ClientTask, T> taskConverter) {
        ArrayList<T> newTasks = new ArrayList<>();

        for (var entry : other.entrySet()) {
            var uuid = entry.getKey();
            var otherTask = entry.getValue();
            var thisTask = cur.get(uuid);

            if (thisTask == null) {
                // Task was added by other side
                newTasks.add(taskConverter.apply(otherTask));
            } else {
                // Task exists on both sides, merge them
                var merged = merge(thisTask, otherTask);
                newTasks.add(merged);
            }
        }
        return newTasks;
    }

    public List<ClientTask> synchronizeClient(Map<UUID, ClientTask> clientTasks, Map<UUID, ClientTask> serverTasks) {
        return synchronize(clientTasks, serverTasks, t -> t);
    }

    public List<ServerTask> synchronizeServer(Map<UUID, ServerTask> serverTasks, Map<UUID, ClientTask> clientTasks, UUID userId) {
        return synchronize(serverTasks, clientTasks, task -> ServerTask.fromClientTask(task, userId));
    }
}
