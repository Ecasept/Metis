package dev.ecasept.unitodo.client.ui.utils;

import dev.ecasept.unitodo.client.DataManager;
import dev.ecasept.unitodo.client.ui.component.TaskTableCellRenderer;
import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.models.db.ClientTask;
import dev.ecasept.unitodo.shared.models.db.TaskState;
import dev.ecasept.unitodo.shared.utils.Log;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Optional;

/**
 * This class provides various functions for configuring and filling the table with the users tasks.
 */
public class FrameUtils {


    /**
     * Creates and configures the JTable that displays the users tasks.
     *
     * @param model the DefaultTableModel containing the tasks displayed by the JTable
     * @return the configured JTable displaying the tasks from the DefaultTableModel.
     */
    public static JTable getConfiguredTable(DefaultTableModel model) {
        JTable taskTable = new JTable(model);




        // Renderer setzen
        taskTable.setDefaultRenderer(Object.class, new TaskTableCellRenderer());

        // Einstellungen für Anzeige des JTable
        taskTable.setShowVerticalLines(false);
        taskTable.setRowHeight(40);
        taskTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        taskTable.getColumnModel().getColumn(0).setMaxWidth(90);
        taskTable.getColumnModel().getColumn(0).setMaxWidth(90);
        taskTable.getColumnModel().getColumn(0).setResizable(false);



        taskTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        taskTable.getColumnModel().getColumn(2).setMaxWidth(100);
        taskTable.getColumnModel().getColumn(2).setMaxWidth(100);
        taskTable.getColumnModel().getColumn(2).setResizable(false);

        taskTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        taskTable.getColumnModel().getColumn(3).setMaxWidth(80);
        taskTable.getColumnModel().getColumn(3).setMaxWidth(80);
        taskTable.getColumnModel().getColumn(3).setResizable(false);

        taskTable.getColumnModel().getColumn(4).setPreferredWidth(50);
        taskTable.getColumnModel().getColumn(4).setMaxWidth(50);
        taskTable.getColumnModel().getColumn(4).setMaxWidth(50);
        taskTable.getColumnModel().getColumn(4).setResizable(false);

        taskTable.getColumnModel().getColumn(5).setPreferredWidth(50);
        taskTable.getColumnModel().getColumn(5).setMaxWidth(50);
        taskTable.getColumnModel().getColumn(5).setMaxWidth(50);
        taskTable.getColumnModel().getColumn(5).setResizable(false);



        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskTable.setColumnSelectionAllowed(false);
        taskTable.setRowSelectionAllowed(false);




        return taskTable;
    }


    /**
     * Fills the passed ArrayList with all tasks with the “Pending” status in ascending order.
     * The DefaultListModel is populated with the representations of the individual tasks as rows in the table.
     *
     *
     *
     * @param tableModel DefaultTableModel, which is populated with the tasks represented as rows in the tableModel.
     * @param dataManager the DataManger-Object from which the tasks are read.
     */
    public static ArrayList<ClientTask> fillListAndTableModelPending(DefaultTableModel tableModel, DataManager dataManager) {
        ArrayList<ClientTask> list;
        try {
            list = dataManager.getTasks(new TaskState.Pending(), new dev.ecasept.unitodo.shared.db.querybuilder.SortOrder.Ascending("dueDate", "dueTime"), false);
        } catch (DatabaseException e) {
            Log.e("Main", "error", e);
            JOptionPane.showMessageDialog(null, "Datenbankfehler! Die Daten konnten nicht korrekt geladen werden", "Datenbankfehler", JOptionPane.ERROR_MESSAGE);
            return new ArrayList<ClientTask>();
        }

        fillTableModel(tableModel, list);

        return list;
    }



    /**
     * Fills the passed ArrayList with all tasks with the “Finished” status in descending order.
     * The DefaultListModel is populated with the representations of the individual tasks as rows in the table.
     *
     *
     *
     * @param tableModel DefaultTableModel, which is populated with the tasks represented as rows in the tableModel.
     * @param dataManager the DataManger-Object from which the tasks are read.
     */
    public static ArrayList<ClientTask> fillListAndTableModelFinished(DefaultTableModel tableModel, DataManager dataManager) {
        ArrayList<ClientTask> list;
        try {
            list = dataManager.getTasks(new TaskState.Finished(null), new dev.ecasept.unitodo.shared.db.querybuilder.SortOrder.Descending("dueDate", "dueTime"), false);
        } catch (DatabaseException e) {
            Log.e("Main", "error", e);
            JOptionPane.showMessageDialog(null, "Datenbankfehler! Die Daten konnten nicht korrekt geladen werden", "Datenbankfehler", JOptionPane.ERROR_MESSAGE);
            return new ArrayList<ClientTask>();
        }

        fillTableModel(tableModel, list);

        return list;
    }

    /**
     * Fills the passed ArrayList with all tasks  in descending order.
     * The DefaultListModel is populated with the representations of the individual tasks as rows in the table.
     *
     * @param tableModel DefaultTableModel, which is populated with the tasks represented as rows in the tableModel.
     * @param dataManager the DataManger-Object from which the tasks are read.
     */
    public static ArrayList<ClientTask> fillListAndTableModelAll(DefaultTableModel tableModel, DataManager dataManager) {
        ArrayList<ClientTask> list;
        try {
            list = dataManager.getTasks(new TaskState.Pending(), new dev.ecasept.unitodo.shared.db.querybuilder.SortOrder.Ascending("dueDate", "dueTime"), false);
            list.addAll(dataManager.getTasks(new TaskState.Finished(null), new dev.ecasept.unitodo.shared.db.querybuilder.SortOrder.Descending("dueDate", "dueTime"), false));
        } catch (DatabaseException e) {
            Log.e("Main", "error", e);
            return new ArrayList<ClientTask>();
        }

        fillTableModel(tableModel, list);

        return list;
    }

    /**
     * Fills the passed ArrayList with all tasks matching the searchString
     *
     * @param tableModel DefaultTableModel, which is populated with the tasks represented as rows in the tableModel.
     * @param dataManager the DataManger-Object from which the tasks are read.
     * @param searchString the users input for the search in the search bar.
     */
    public static ArrayList<ClientTask> fillListAndTableModelSearched(DefaultTableModel tableModel, DataManager dataManager, String searchString) {
        ArrayList<ClientTask> list;
        try {
            ArrayList<ClientTask> allSearch = dataManager.searchTasks(searchString);

            ArrayList<ClientTask> pendingTasks = new ArrayList<>();
            ArrayList<ClientTask> finishedTasks = new ArrayList<>();

            for (ClientTask t : allSearch) {
                if (t.getState().isPending()) {
                    pendingTasks.add(t);
                } else {
                    finishedTasks.add(t);
                }
            }

            pendingTasks.sort((t1, t2) -> t1.getDueDate().compareTo(t2.getDueDate()));
            finishedTasks.sort((t1, t2) -> t2.getDueDate().compareTo(t1.getDueDate()));

            list = new ArrayList<>(pendingTasks);
            list.addAll(finishedTasks);

        } catch (DatabaseException e) {
            Log.e("Main", "error", e);
            JOptionPane.showMessageDialog(null, "Datenbankfehler! Die Daten konnten nicht korrekt geladen werden", "Datenbankfehler", JOptionPane.ERROR_MESSAGE);
            return new ArrayList<ClientTask>();
        }

        fillTableModel(tableModel, list);

        return list;
    }



    private static void fillTableModel(DefaultTableModel tableModel, ArrayList<ClientTask> list) {

        String str = "";

        for (ClientTask t : list) {
            LocalDate date = t.getDueDate();
            Optional<LocalTime> timeOptional = t.getDueTime();
            if (timeOptional.isPresent()) {
                LocalTime time = timeOptional.get();
                int min = time.getMinute();
                String minStr;
                if (min < 10) {
                    minStr = "0" + min;
                } else {
                    minStr = "" + min;
                }
                str = date.getDayOfMonth() + "." + date.getMonthValue() + "." + date.getYear() + " " + time.getHour() + ":" + minStr;
            } else {
                str = date.getDayOfMonth() + "." + date.getMonthValue() + "." + date.getYear();
            }
            String stateStr = t.getState().isPending() ? "Ausstehend" : "Erledigt";
            tableModel.addRow(new Object[]{stateStr, t.getTitle(), str, t.getPriority(), "", ""});
        }
    }
}
