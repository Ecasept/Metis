package dev.ecasept.unitodo.shared.models.db;

public enum TaskPriority {
    High (0),
    Mid (1),
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
