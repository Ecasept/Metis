package dev.ecasept.unitodo.shared.utils;

import dev.ecasept.unitodo.models.Task;

public class Log {
        public static final LogLevel LOG_LEVEL = LogLevel.DEBUG;
        public static void d(String tag, Object message) {
            System.out.println(Color.g("[" + tag + "] " + message.toString()));
        }
        public static void w(String tag, Object message) {
            System.out.println(Color.y("[" + tag + "] " + message.toString()));
        }
        public static void i(String tag, Object message) {
            System.out.println(Color.lb("[" + tag + "] " + message.toString()));
        }
        public static void e(String tag, Object message, Throwable t) {
            System.err.println(Color.r("[" + tag + "] " + message.toString()));
            System.err.println(Color.r("Error: " + t.getClass().getName() + ": " + t.getMessage()));
            t.printStackTrace(System.err);
        }
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