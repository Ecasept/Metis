package dev.ecasept.unitodo.shared.utils;


import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.UnaryOperator;

/**
 * A simple logging utility that supports different log levels and colored output.
 */
public class Log {
    /** The current log level. Only messages at this level or higher will be printed. */
    public static final LogLevel LOG_LEVEL = LogLevel.DEBUG;

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private static void log(String tag, String message, UnaryOperator<String> colorer, String levelName) {
        String sb = "[" + LocalTime.now().format(TIME_FORMAT) + "] " +
                "[" + levelName + "] " +
                "[" + tag + "] " +
                message;

        System.out.println(colorer.apply(sb));
    }

    /**
     * Logs a debug message with the given tag and message. The message will only be printed if the current log level is {@link LogLevel#DEBUG} or higher.
     * @param tag The tag to associate with the log message (e.g. the class name or module name).
     * @param message The message to log. This can be any object, and its {@code toString()} method will be called to get the string representation of the message.
     */
    public static void d(String tag, Object message) {
        if (LogLevel.DEBUG.isAtLeast(LOG_LEVEL)) {
            log(tag, message.toString(), Color::g, "DEBUG");
        }
    }

    /**
     * Logs a warning message with the given tag and message. The message will only be printed if the current log level is {@link LogLevel#WARNING} or higher.
     * @param tag The tag to associate with the log message (e.g. the class name or module name).
     * @param message The message to log. This can be any object, and its {@code toString()} method will be called to get the string representation of the message.
     */
    public static void w(String tag, Object message) {
        if (LogLevel.WARNING.isAtLeast(LOG_LEVEL)) {
            log(tag, message.toString(), Color::y, "WARNING");
        }
    }

    /**
     * Logs a warning message with the given tag, message and throwable. The message will only be printed if the current log level is {@link LogLevel#WARNING} or higher. The throwable's stack trace will also be printed to standard output.
     * @param tag The tag to associate with the log message (e.g. the class name or module name).
     * @param message The message to log. This can be any object, and its {@code toString()} method will be called to get the string representation of the message.
     * @param t The throwable to log. The stack trace of this throwable will be printed to standard error along with the message.
     */
    public static void w(String tag, Object message, Throwable t) {
        if (LogLevel.WARNING.isAtLeast(LOG_LEVEL)) {
            log(tag, message.toString(), Color::y, "WARNING");
            System.out.println(Color.y("Cause: " + t.getClass().getName() + ": " + t.getMessage()));
            t.printStackTrace(System.out);
        }
    }

    /**
     * Logs an info message with the given tag and message. The message will only be printed if the current log level is {@link LogLevel#INFO} or higher.
     * @param tag The tag to associate with the log message (e.g. the class name or module name).
     * @param message The message to log. This can be any object, and its {@code toString()} method will be called to get the string representation of the message.
     */
    public static void i(String tag, Object message) {
        if (LogLevel.INFO.isAtLeast(LOG_LEVEL)) {
            log(tag, message.toString(), Color::lb, "INFO");
        }
    }

    /**
     * Logs an error message with the given tag, message, and throwable. The message will only be printed if the current log level is {@link LogLevel#ERROR} or higher. The throwable's stack trace will also be printed to standard error.
     * @param tag The tag to associate with the log message (e.g. the class name or module name).
     * @param message The message to log. This can be any object, and its {@code toString()} method will be called to get the string representation of the message.
     * @param t The throwable to log. The stack trace of this throwable will be printed to standard error along with the message.
     */
    public static void e(String tag, Object message, Throwable t) {
        if (LogLevel.ERROR.isAtLeast(LOG_LEVEL)) {
            log(tag, message.toString(), Color::r, "ERROR");
            System.err.println(Color.r("Cause: " + t.getClass().getName() + ": " + t.getMessage()));
            t.printStackTrace(System.err);
        }
    }

    /**
     * Logs an error message with the given tag and message. The message will only be printed if the current log level is {@link LogLevel#ERROR} or higher.
     * @param tag The tag to associate with the log message (e.g. the class name or module name).
     * @param message The message to log. This can be any object, and its {@code toString()} method will be called to get the string representation of the message.
     */
    public static void e(String tag, Object message) {
        if (LogLevel.ERROR.isAtLeast(LOG_LEVEL)) {
            log(tag, message.toString(), Color::r, "ERROR");
        }
    }

    /**
     * Formats a byte array as a string of hexadecimal values. Each byte will be represented as two hexadecimal digits, and bytes will be separated by spaces. The entire byte array will be enclosed in square brackets. For example, the byte array {0x01, 0x2A, 0xFF} would be formatted as "[01 2A FF]".
     * @param bytes The byte array to format as a string.
     * @return A string representation of the byte array in hexadecimal format.
     */
    public static String formatBytes(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < bytes.length; i++) {
            sb.append(String.format("%02X", bytes[i]));
            if (i < bytes.length - 1) sb.append(" ");
        }
        sb.append("]");
        return sb.toString();
    }
}