package dev.ecasept.unitodo.shared.utils;


/**
 * Utility class for coloring text in the console.
 */
public class Color {
    /**
     * Colors the given text in light blue.
     * @param text The text to color.
     * @return The colored text.
     */
    public static String lb(String text) {
        return "\u001B[34m" + text + "\u001B[0m";
    }
    /**
     * Colors the given text in yellow.
     * @param text The text to color.
     * @return The colored text.
     */
    public static String y(String text) {
        return "\u001B[33m" + text + "\u001B[0m";
    }
    /**
     * Colors the given text in red.
     * @param text The text to color.
     * @return The colored text.
     */
    public static String r(String text) {
        return "\u001B[31m" + text + "\u001B[0m";
    }

    /**
     * Colors the given text in green.
     * @param text The text to color.
     * @return The colored text.
     */
    public static String g(String text) {
        return "\u001B[32m" + text + "\u001B[0m";
    }
}
