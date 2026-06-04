package dev.ecasept.unitodo.models;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TaskManager {
    private ArrayList<Task> tasks;

    public TaskManager() {
        tasks = new ArrayList<>();
    }

    public void addTask(Task task) {
        tasks.add(task);
    }

    public Task getTask(int index) {
        return tasks.get(index);
    }

    public void removeTask(int index) {
        tasks.remove(index);
    }

    public void removeTask(Task task) {
        tasks.remove(task);
    }

    public ArrayList<Task> getAllTasks() {
        return tasks;
    }

    /**
     * Filtert die Aufgabenliste anhand einer übergebenen Bedingung.
     * * Beispielaufruf für alle ausstehenden Aufgaben:
     * taskManager.getFilteredTasks(task -> task.getState() == TaskState.Pending);
     *
     * @param condition Die Filterbedingung als Lambda-Ausdruck.
     * @return Eine neue Liste mit allen Aufgaben, die die Bedingung erfüllen.
     */
    public ArrayList<Task> getFilteredTasks(Predicate<Task> condition) {
        return tasks.stream()
                .filter(condition)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Sortiert die Aufgabenliste anhand einer übergebenen Sortierung.
     * * Beispielaufruf für nach Name sortiert:
     * taskManager.getSortedTasks(Comparator.comparing(Task::getName));
     *
     * @param sort Die Sortierung als Comparator.
     * @return Eine neue Liste mit allen Aufgaben, nach der Sortierung.
     */
    public ArrayList<Task> getSortedTasks(Comparator<Task> sort) {
        return tasks.stream()
                .sorted(sort)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Filtert und sortiert die Aufgabenliste anhand einer übergebenen Bedingung und einer Sortierung.
     * Beispielaufruf für alle Aufgaben mit Datum, sortiert nach dem Fälligkeitsdatum:
     * taskManager.getFilteredSortedTasks(
     * task -> task.getDueDate().isPresent(),
     * Comparator.comparing(task -> task.getDueDate().get())
     * );
     *
     * @param condition Die Bedingung als Lambda-Ausdruck.
     * @param sort Die Sortierung als Comparator.
     * @return Eine neue Liste mit allen Aufgaben, die die Bedingung erfüllen, nach der Sortierung.
     */
    public ArrayList<Task> getFilteredSortedTasks(Predicate<Task> condition, Comparator<Task> sort) {
        return tasks.stream()
                .filter(condition)
                .sorted(sort)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
