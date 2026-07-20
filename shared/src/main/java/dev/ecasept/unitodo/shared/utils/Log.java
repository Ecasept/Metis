package dev.ecasept.unitodo.shared.utils;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.UnaryOperator;

/**
 * A simple logging utility that supports different log levels and colored output.
 */
public class Log {
    /** The current log level. Only messages at this level or higher will be printed. */
    public static final LogLevel LOG_LEVEL = LogLevel.DEBUG;

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss.SSS", Locale.US);

    private static void log(String tag, String message, UnaryOperator<String> colorer, LogLevel level) {
        log(tag, message, colorer, level, "");
    }

    private static void log(String tag, String message, UnaryOperator<String> colorer, LogLevel level, String cause) {
        if (!level.isAtLeast(LOG_LEVEL)) {
            return;
        }
        var sb = new StringBuilder();
        sb.append("[")
                .append(LocalDateTime.now().format(TIME_FORMAT))
                .append("] [")
                .append(level)
                .append("] [")
                .append(Thread.currentThread().getName())
                .append("] [")
                .append(tag)
                .append("] ")
                .append(message);
        if (cause != null && !cause.isEmpty()) {
            sb.append("\n").append(cause);
        }

        var stream = switch (level) {
            case ERROR, WARNING -> System.err;
            default -> System.out;
        };

        stream.println(colorer.apply(sb.toString()));
        stream.flush();
    }

    /**
     * Logs a debug message with the given tag and message. The message will only be printed if the current log level is {@link LogLevel#DEBUG} or higher.
     * @param tag The tag to associate with the log message (e.g. the class name or module name).
     * @param message The message to log. This can be any object, and its {@code toString()} method will be called to get the string representation of the message.
     */
    public static void d(String tag, Object message) {
        log(tag, message.toString(), Color::g, LogLevel.DEBUG);
    }

    /**
     * Logs a warning message with the given tag and message. The message will only be printed if the current log level is {@link LogLevel#WARNING} or higher.
     * @param tag The tag to associate with the log message (e.g. the class name or module name).
     * @param message The message to log. This can be any object, and its {@code toString()} method will be called to get the string representation of the message.
     */
    public static void w(String tag, Object message) {
        log(tag, message.toString(), Color::y, LogLevel.WARNING);
    }

    /**
     * Logs a warning message with the given tag, message and throwable. The message will only be printed if the current log level is {@link LogLevel#WARNING} or higher. The throwable's stack trace will also be printed to standard output.
     * @param tag The tag to associate with the log message (e.g. the class name or module name).
     * @param message The message to log. This can be any object, and its {@code toString()} method will be called to get the string representation of the message.
     * @param t The throwable to log. The stack trace of this throwable will be printed to standard error along with the message.
     */
    public static void w(String tag, Object message, Throwable t) {
            log(tag, message.toString(), Color::y, LogLevel.WARNING, formatThrowable(t));
    }

    /**
     * Logs an info message with the given tag and message. The message will only be printed if the current log level is {@link LogLevel#INFO} or higher.
     * @param tag The tag to associate with the log message (e.g. the class name or module name).
     * @param message The message to log. This can be any object, and its {@code toString()} method will be called to get the string representation of the message.
     */
    public static void i(String tag, Object message) {
        log(tag, message.toString(), Color::lb, LogLevel.INFO);
    }

    /**
     * Logs an error message with the given tag, message, and throwable. The message will only be printed if the current log level is {@link LogLevel#ERROR} or higher. The throwable's stack trace will also be printed to standard error.
     * @param tag The tag to associate with the log message (e.g. the class name or module name).
     * @param message The message to log. This can be any object, and its {@code toString()} method will be called to get the string representation of the message.
     * @param t The throwable to log. The stack trace of this throwable will be printed to standard error along with the message.
     */
    public static void e(String tag, Object message, Throwable t) {
        log(tag, message.toString(), Color::r, LogLevel.ERROR, formatThrowable(t));
    }


    private static String formatThrowable(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    /**
     * Logs an error message with the given tag and message. The message will only be printed if the current log level is {@link LogLevel#ERROR} or higher.
     * @param tag The tag to associate with the log message (e.g. the class name or module name).
     * @param message The message to log. This can be any object, and its {@code toString()} method will be called to get the string representation of the message.
     */
    public static void e(String tag, Object message) {
        log(tag, message.toString(), Color::r, LogLevel.ERROR);
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
