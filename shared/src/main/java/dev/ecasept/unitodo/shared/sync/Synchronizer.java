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
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

/** Shared synchronization logic */
public class Synchronizer {

    /** Returns the newer of two TimestampedField values, preferring the server's value in case of a tie */
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

    /** Resolves whether to delete a task when one side has deleted it and the other might have edited it */
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

    /** Merges the data of two different tasks on a field level, smartly resolving issues like deletion */
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


    /** Merges a set of changes from one side into the current set of tasks.
     *
     * @param cur Our tasks
     * @param other The delta from the other side
     * @param taskConverter A way to convert the other side's task type into our system's task type
     * @param taskMerger Merges our task with the incoming task, preserving server-first conflict resolution
     * @return A list of merged tasks
     * @param <T> The type of task that our system uses
     */
    private <T extends Task<T>> List<T> synchronize(Map<UUID, T> cur, Map<UUID, ClientTask> other, Function<ClientTask, T> taskConverter, BiFunction<T, ClientTask, T> taskMerger) {
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
                var merged = taskMerger.apply(thisTask, otherTask);
                newTasks.add(merged);
            }
        }
        return newTasks;
    }

    /** Merges a delta received from the server into the client's set of tasks */
    public List<ClientTask> synchronizeClient(Map<UUID, ClientTask> clientTasks, Map<UUID, ClientTask> serverTasks) {
        return synchronize(clientTasks, serverTasks, t -> t, (client, server) -> merge(server, client));
    }

    /** Merges a delta received from the client into the server's set of tasks */
    public List<ServerTask> synchronizeServer(Map<UUID, ServerTask> serverTasks, Map<UUID, ClientTask> clientTasks, UUID userId) {
        return synchronize(serverTasks, clientTasks, task -> ServerTask.fromClientTask(task, userId), this::merge);
    }
}
