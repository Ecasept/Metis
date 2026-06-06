package dev.ecasept.unitodo.shared.sync;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import dev.ecasept.unitodo.shared.models.db.Task;
import dev.ecasept.unitodo.shared.models.db.TimestampedField;

public class Synchronizer {
    private <T> TimestampedField<T> pickNewer(TimestampedField<T> a, TimestampedField<T> b) {
        var aDate = a.getLastUpdated();
        var bDate = b.getLastUpdated();
        if (aDate.isAfter(bDate)) {
            return a;
        } else {
            return b;
        }
    }
    private Task merge(Task a, Task b) {
        var title = pickNewer(a.title(), b.title());
        var description = pickNewer(a.description(), b.description());
        var state = pickNewer(a.state(), b.state());
        var priority = pickNewer(a.priority(), b.priority());
        var dueDate = pickNewer(a.dueDate(), b.dueDate());
        return new Task(a.uuid(), title, description, state, priority, dueDate, false, null);
    }

    public List<Task> synchronize(Map<UUID, Task> cur, Map<UUID, Task> other) {
        ArrayList<Task> newTasks = new ArrayList<>();

        for (var entry : other.entrySet()) {
            var uuid = entry.getKey();
            var otherTask = entry.getValue();
            var thisTask = cur.get(uuid);

            if (thisTask == null) {
                // Task was added by other
                newTasks.add(otherTask);
            } else if (thisTask.isDeleted() && otherTask.isDeleted()) {
                // Deleted by both, so irrelevant which one we use
                newTasks.add(thisTask);
            } else if (thisTask.isDeleted() || otherTask.isDeleted()) {
                var deletedTask = thisTask.isDeleted() ? thisTask : otherTask;
                var preservedTask = thisTask.isDeleted() ? otherTask : thisTask;

                var deletedDate = deletedTask.deletedAt();
                var lastEdited = preservedTask.getLastUpdate();
                if (deletedDate.isAfter(lastEdited)) {
                    // Deleted after last edit, so deletion should be preserved
                    newTasks.add(deletedTask);
                } else {
                    // Deleted before last edit, so edit should resurrect the task
                    newTasks.add(preservedTask);
                }
            } else {
                // Both edited, so we need to merge
                newTasks.add(merge(thisTask, otherTask));
            }
        }
        return newTasks;
    }
}
