package dev.ecasept.unitodo.shared.models.db;

import dev.ecasept.unitodo.shared.serialization.annotations.Field;
import dev.ecasept.unitodo.shared.serialization.annotations.SerialInstance;
import dev.ecasept.unitodo.shared.serialization.annotations.Serializable;

import java.time.LocalDateTime;
import java.util.Optional;

@Serializable
public sealed interface TaskState permits TaskState.Finished, TaskState.Pending {
    /** Returns the state as an unambiguous integer */
    int toInt();

    /**
     * Represents a state in a finished state.
     * @param completedAt When this state was completed
     */
    @SerialInstance(tag=1)
    @Serializable
    record Finished(@Field(tag=1) LocalDateTime completedAt) implements TaskState {

        @Override
        public int toInt() {
            return 0;
        }
    }

    /**
     * Represents a state in an unfinished state.
     */
    @SerialInstance(tag=2)
    @Serializable
    record Pending() implements TaskState {
        @Override
        public int toInt() {
            return 1;
        }
    }

    /** Whether this state represents a finished task */
    default boolean isFinished() {
        return this instanceof Finished;
    }
    /** Whether this state represents an unfinished task */
    default boolean isPending() {
        return this instanceof Pending;
    }

    /** Returns the date and time this task was completed at, if it has been */
    default Optional<LocalDateTime> getCompletedAt() {
        return this instanceof Finished(var completedAt)
                ? Optional.of(completedAt)
                : Optional.empty();
    }

    /** Converts back from the integer representation obtained through {@link TaskState#toInt()} */
    static TaskState fromInt(int value, LocalDateTime completedAt) {
        return switch (value) {
            case 0 -> new Finished(completedAt);
            case 1 -> new Pending();
            default -> throw new IllegalArgumentException("Invalid TaskState value: " + value);
        };
    }
}
