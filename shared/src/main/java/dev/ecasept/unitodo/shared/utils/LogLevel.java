package dev.ecasept.unitodo.shared.utils;

public enum LogLevel {
    DEBUG,
    INFO,
    WARNING,
    ERROR;

    /**
     * Returns true if this log level is at least as severe as the other log level.
     * @param other The other log level to compare against.
     * @return true if this log level is at least as severe as the other log level, false otherwise.
     */
    public boolean isAtLeast(LogLevel other) {
        return this.ordinal() >= other.ordinal();
    }
}
