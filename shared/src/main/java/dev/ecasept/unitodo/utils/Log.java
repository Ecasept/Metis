package dev.ecasept.unitodo.utils;

public class Log {
        public static final LogLevel LOG_LEVEL = LogLevel.DEBUG;
        public static void d(String tag, String message) {
            System.out.println(Color.g("[" + tag + "] " + message));
        }
        public static void w(String tag, String message) {
            System.out.println(Color.y("[" + tag + "] " + message));
        }
        public static void i(String tag, String message) {
            System.out.println(Color.lb("[" + tag + "] " + message));
        }
        public static void e(String tag, String message, Throwable t) {
            System.err.println(Color.r("[" + tag + "] " + message));
            System.err.println(Color.r("Error: " + t.getClass().getName() + ": " + t.getMessage()));
            t.printStackTrace(System.err);
        }
}