package dev.ecasept.unitodo.client.ui.utils;

import javax.swing.*;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TimeUtils {


    /**
     * This method checks if the input dueDateString is a valid dueDate for a taks.
     * @param dueDateString the String the user entered in the date field.
     * @return the dueDate the user entered if the date is valid or null otherwise
     */
    public static LocalDate checkDueDate(String dueDateString) {
        // Prüfen ob keine Eingabe erfolgt ist
        if (dueDateString.equals("dd.mm.yyyy")) {
            JOptionPane.showMessageDialog(null, "Ungültige Eingabe:\nDas Fälligkeitsdatum darf nicht leer sein.");
            return null;
        }

        String[] dueDateArr = dueDateString.split("[.]");
        if (dueDateArr.length != 3) {
            JOptionPane.showMessageDialog(null, "Ungültige Eingabe:\nDas Fälligkeitsdatum hat ein ungültiges Format.");
            return null;
        }


        int day = 0;
        int month = 0;
        int year = 0;


        try {
            day = Integer.parseInt(dueDateArr[0]);
            month = Integer.parseInt(dueDateArr[1]);
            year = Integer.parseInt(dueDateArr[2]);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Ungültige Eingabe:\nDas Fälligkeitsdatum hat ein ungültiges Format.");
            return null;
        }

        LocalDate dueDate;
        try {
            dueDate = LocalDate.of(year, month, day);
        } catch (DateTimeException dateEx) {
            JOptionPane.showMessageDialog(null, "Ungültige Eingabe:\nDie eingegebenen Zahlen stellen kein gültiges Datum dar.");
            return null;
        }

        if (dueDate.isBefore(LocalDate.now())) {
            JOptionPane.showMessageDialog(null, "Ungültige Eingabe:\nDas gewählt Fälligkeitsdatum liegt in der Vergangenheit.");
            return null;
        }

        return dueDate;
    }




    /**
     * This method checks if the input dueTimeString is a valid dueTime for a taks.
     * @param dueTimeString the String the user entered in the date field.
     *
     * @return the dueDate the user entered if the date is valid or null otherwise
     */
    /**
     * This method checks if the input dueTimeString is a valid dueTime for a taks.
     * @param dueTimeString the String the user entered in the time field.
     * @param dueDate the dueDate the user entered for the task (it depends on the dueDate wether a due time is valid or not)
     * @return the dueTime entered by the user if the time is valid and throws an IllegalArgumentException otherwise
     * @throws IllegalArgumentException the exception is thrown if the dueTimeString is an invalid format or time.
     */
    public static LocalTime checkDueTime(String dueTimeString, LocalDate dueDate) throws IllegalArgumentException {

        String[] dueDateArr = dueTimeString.split("[:]");
        if (dueDateArr.length != 2) {
            JOptionPane.showMessageDialog(null, "Ungültige Eingabe:\nDie Fälligkeitsuhrzeit hat ein ungültiges Format.");
            throw new IllegalArgumentException("Illegal Time Format");
        }


        int hour = -1;
        int minute = -1;



        try {
            hour = Integer.parseInt(dueDateArr[0]);
            minute = Integer.parseInt(dueDateArr[1]);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Ungültige Eingabe:\nDie Fälligkeitsuhrzeit hat ein ungültiges Format.");
            throw new IllegalArgumentException("Illegal Time Format");
        }

        LocalTime dueTime;
        try {
            dueTime = LocalTime.of(hour, minute);
        } catch (DateTimeException dateEx) {
            JOptionPane.showMessageDialog(null, "Ungültige Eingabe:\nDie eingegebenen Zahlen stellen keine gültige Uhrzeit dar.");
            throw new IllegalArgumentException("Illegal Time Format");
        }


        if (LocalDateTime.of(dueDate, dueTime).isBefore(LocalDateTime.now())) {
            JOptionPane.showMessageDialog(null, "Ungültige Eingabe:\nDie gewählte Fälligkeitsuhrzeit liegt in der Vergangenheit.");
            throw new IllegalArgumentException("Illegal Time Format");
        }

        return dueTime;
    }



}
