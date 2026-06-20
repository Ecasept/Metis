package dev.ecasept.unitodo.server.api;

import dev.ecasept.unitodo.shared.models.db.ServerTask;
import dev.ecasept.unitodo.shared.models.db.ClientTask;
import dev.ecasept.unitodo.shared.models.db.TimestampedField;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ServerSynchronizer {

    private <V> TimestampedField<V> pickNewer(TimestampedField<V> a, TimestampedField<V> b) {
        var aDate = a.getLastUpdated();
        var bDate = b.getLastUpdated();
        if (aDate.isAfter(bDate)) {
            return a;
        } else {
            return b;
        }
    }

    private ServerTask merge(ServerTask a, ClientTask b) {
        var title = pickNewer(a.title(), b.title());
        var description = pickNewer(a.description(), b.description());
        var state = pickNewer(a.state(), b.state());
        var priority = pickNewer(a.priority(), b.priority());
        var dueDate = pickNewer(a.dueDate(), b.dueDate());
        var dueTime = pickNewer(a.dueTime(), b.dueTime());
        var isDeleted = pickNewer(a.isDeleted(), b.isDeleted());
        return new ServerTask(a.uuid(), title, description, state, priority, dueDate, dueTime, isDeleted, a.userId());
    }

    public List<ServerTask> synchronize(Map<UUID, ServerTask> cur, Map<UUID, ClientTask> other, UUID userId) {
        ArrayList<ServerTask> newTasks = new ArrayList<>();

        for (var entry : other.entrySet()) {
            var uuid = entry.getKey();
            var otherTask = entry.getValue();
            var thisTask = cur.get(uuid);

            if (thisTask == null) {
                // Task was added by client
                newTasks.add(ServerTask.fromClientTask(otherTask, userId));
            } else {
                // Task exists on both sides, merge them
                var merged = merge(thisTask, otherTask);
                newTasks.add(merged);
            }
        }
        return newTasks;
    }
}
