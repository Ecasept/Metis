package dev.ecasept.unitodo.shared.utils;

public class Color {
    public static String lb(String text) {
        return "\u001B[34m" + text + "\u001B[0m";
    }
    public static String y(String text) {
        return "\u001B[33m" + text + "\u001B[0m";
    }
    public static String r(String text) {
        return "\u001B[31m" + text + "\u001B[0m";
    }
     public static String g(String text) {
        return "\u001B[32m" + text + "\u001B[0m";
    }
}
