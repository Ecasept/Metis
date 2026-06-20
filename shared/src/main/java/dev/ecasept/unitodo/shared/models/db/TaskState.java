package dev.ecasept.unitodo.shared.models.db;

import java.time.LocalDateTime;
import java.util.Optional;

public sealed interface TaskState permits TaskState.Finished, TaskState.Pending {
    int toInt();

    record Finished(LocalDateTime completedAt) implements TaskState {
        @Override
        public int toInt() {
            return 0;
        }
    }
    record Pending() implements TaskState {
        @Override
        public int toInt() {
            return 1;
        }
    }

    default boolean isFinished() {
        return this instanceof Finished;
    }
    default boolean isPending() {
        return this instanceof Pending;
    }

    default Optional<LocalDateTime> getCompletedAt() {
        return this instanceof Finished(var completedAt)
                ? Optional.of(completedAt)
                : Optional.empty();
    }


    static TaskState fromInt(int value, LocalDateTime completedAt) {
        return switch (value) {
            case 0 -> new Finished(completedAt);
            case 1 -> new Pending();
            default -> throw new IllegalArgumentException("Invalid TaskState value: " + value);
        };
    }
}
