package dev.ecasept.unitodo.shared.models.db;

import dev.ecasept.unitodo.shared.serialization.annotations.SerialInstance;
import dev.ecasept.unitodo.shared.serialization.annotations.Serializable;

/** Ranking for how important a task is */
@Serializable
public enum TaskPriority {
    @SerialInstance(tag=1)
    High (0),
    @SerialInstance(tag=2)
    Mid (1),
    @SerialInstance(tag=3)
    Low (2);


    private final int value;
    TaskPriority(int value) {
        this.value = value;
    }

    public int toInt() {
        return value;
    }

    public static TaskPriority fromInt(int value) {
        return switch (value) {
            case 0 -> High;
            case 1 -> Mid;
            case 2 -> Low;
            default -> throw new IllegalArgumentException("Invalid TaskPriority value: " + value);
        };
    }
}
