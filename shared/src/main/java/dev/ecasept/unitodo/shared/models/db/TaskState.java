package dev.ecasept.unitodo.shared.models.db;

public enum TaskState {
    Finished (0),
    Pending (1);

    private final int value;
    TaskState(int value) {
        this.value = value;
    }

    public int toInt() {
        return value;
    }

    public static TaskState fromInt(int value) {
        return switch (value) {
            case 0 -> Finished;
            case 1 -> Pending;
            default -> throw new IllegalArgumentException("Invalid TaskState value: " + value);
        };
    }
}
