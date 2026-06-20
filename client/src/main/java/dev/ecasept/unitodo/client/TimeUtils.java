package dev.ecasept.unitodo.client;

import javax.swing.*;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TimeUtils {



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




    public static LocalTime checkDueTime(String dueTimeString, LocalDate dueDate) {

        String[] dueDateArr = dueTimeString.split("[:]");
        if (dueDateArr.length != 2) {
            JOptionPane.showMessageDialog(null, "Ungültige Eingabe:\nDie Fälligkeitsuhrzeit hat ein ungültiges Format.");
            return null;
        }


        int hour = -1;
        int minute = -1;



        try {
            hour = Integer.parseInt(dueDateArr[0]);
            minute = Integer.parseInt(dueDateArr[1]);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Ungültige Eingabe:\nDie Fälligkeitsuhrzeit hat ein ungültiges Format.");
            return null;
        }

        LocalTime dueTime;
        try {
            dueTime = LocalTime.of(hour, minute);
        } catch (DateTimeException dateEx) {
            JOptionPane.showMessageDialog(null, "Ungültige Eingabe:\nDie eingegebenen Zahlen stellen keine gültige Uhrzeit dar.");
            return null;
        }


        if (LocalDateTime.of(dueDate, dueTime).isBefore(LocalDateTime.now())) {
            JOptionPane.showMessageDialog(null, "Ungültige Eingabe:\nDie gewählte Fälligkeitsuhrzeit liegt in der Vergangenheit.");
            return null;
        }

        return dueTime;
    }



}
