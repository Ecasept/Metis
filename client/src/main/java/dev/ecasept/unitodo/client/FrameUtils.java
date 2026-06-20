package dev.ecasept.unitodo.client;

import dev.ecasept.unitodo.client.db.ClientDatabaseRepository;
import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.models.db.ClientTask;
import dev.ecasept.unitodo.shared.models.db.TaskState;
import dev.ecasept.unitodo.shared.utils.Log;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Optional;

public class FrameUtils {



    public static JTable getConfiguredTable(DefaultTableModel model) {
        JTable taskTable = new JTable(model);




        // Renderer und Editor setzen
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
     * Befüllt die übergebene ArrayList mit allen Tasks mit Status Pending in aufsteigener Reihenfolge.
     * Die DefaultListModel wird mit den Repräsentationen der einzelnen Tasks als Zeilen in der Tabelle gefüllt.
     *
     *
     *
     * @param tableModel DefaultTableModel das mit den Repräsentationen der Tasks in list als Tabellenzeilen gefüllt wird.     *
     * @param dataManager Datenbankinstanz aus der die Daten gelesen werden
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
     * Befüllt die übergebene ArrayList mit allen Tasks mit Status Finishes in absteigender Reihenfolge.
     * Die DefaultListModel wird mit den Repräsentationen der einzelnen Tasks als Zeilen in der Tabelle gefüllt.
     *
     *
     *
     * @param tableModel DefaultTableModel das mit den Repräsentationen der Tasks in list als Tabellenzeilen gefüllt wird.     *
     * @param dataManager Datenbankinstanz aus der die Daten gelesen werden
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
     * Befüllt die übergebene ArrayList mit allen Tasks in absteigender Reihenfolge.
     * Die DefaultListModel wird mit den Repräsentationen der einzelnen Tasks als Zeilen in der Tabelle gefüllt.
     *
     * @param tableModel DefaultTableModel das mit den Repräsentationen der Tasks in list als Tabellenzeilen gefüllt wird.     *
     * @param dataManager Datenbankinstanz aus der die Daten gelesen werden
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
     * Befüllt die übergebene DefaultTableModel mit allen Tasks, die dem Suchstring entsprechen.
     *
     * @param tableModel DefaultTableModel das mit den Repräsentationen der Tasks gefüllt wird.
     * @param dataManager Datenbankinstanz aus der die Daten gelesen werden
     * @param searchString Der Suchbegriff
     */
    public static ArrayList<ClientTask> fillListAndTableModelSearched(DefaultTableModel tableModel, DataManager dataManager, String searchString) {
        ArrayList<ClientTask> list;
        try {
            ArrayList<ClientTask> allSearch = dataManager.searchTasks(searchString);

            ArrayList<ClientTask> pendingTasks = new ArrayList<>();
            ArrayList<ClientTask> finishedTasks = new ArrayList<>();

            for (ClientTask t : allSearch) {
                if (t.state().get().isPending()) {
                    pendingTasks.add(t);
                } else {
                    finishedTasks.add(t);
                }
            }

            pendingTasks.sort((t1, t2) -> t1.dueDate().get().compareTo(t2.dueDate().get()));
            finishedTasks.sort((t1, t2) -> t2.dueDate().get().compareTo(t1.dueDate().get()));

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
            LocalDate date = t.dueDate().get();
            Optional<LocalTime> timeOptional = t.dueTime().get();
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
            String stateStr = t.state().get().isPending() ? "Ausstehend" : "Erledigt";
            tableModel.addRow(new Object[]{stateStr, t.title().get(), str, t.priority().get(), "", ""});
        }
    }
}
