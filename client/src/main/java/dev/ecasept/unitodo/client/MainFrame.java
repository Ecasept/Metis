package dev.ecasept.unitodo.client;

import dev.ecasept.unitodo.client.db.DatabaseRepository;
import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.db.SortOrder;
import dev.ecasept.unitodo.shared.models.db.Task;
import dev.ecasept.unitodo.shared.models.db.TaskPriority;
import dev.ecasept.unitodo.shared.models.db.TaskState;
import dev.ecasept.unitodo.shared.utils.Log;

import javax.swing.*;
import javax.swing.plaf.metal.MetalIconFactory;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

@SuppressWarnings("LanguageDetectionInspection")
public class MainFrame extends JFrame {

    private final DatabaseRepository db;

    // Elemente der GUI
    private JScrollPane scrollPaneTasks;
    private DefaultListModel<String> titles;
    JPanel mainPanelLeft;
    JPanel mainPanelRight;

    // Buttons
    private JButton logout;
    private JButton delAcc;
    private JButton sync;
    private JButton showPending;
    private JButton showFinished;
    private JButton newTask;

    // Listener für die Button
    private ActionListener showPendingListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            showPending();

        }
    };
    private ActionListener showFinishedListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            showFinished();

        }
    };
    private ActionListener newTaskListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            showAddTask();
        }
    };




    public MainFrame(DatabaseRepository db) {
        this.db = db;

        // Frame vorbereiten
        this.setTitle("To-Do Liste");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800, 450);
        this.setBackground(Color.lightGray);
        this.setLocationRelativeTo(null);

        setOverview();

        this.setVisible(true);
    }

    public void setOverview() {
        this.getContentPane().removeAll();

        // Panels für Buttons in Seitenleiste erstellen
        // Panel für Buttons oben links
        JPanel panelUp = new JPanel();
        panelUp.setLayout(new BoxLayout(panelUp, BoxLayout.Y_AXIS));
        logout = new JButton("Abmelden");
        delAcc = new JButton("Account löschen");
        panelUp.add(logout);
        panelUp.add(delAcc);

        // Panel für Buttons in der Mitte
        JPanel panelMid = new JPanel();
        panelMid.setLayout(new BoxLayout(panelMid, BoxLayout.Y_AXIS));
        sync = new JButton("Synchronisieren");
        showPending = new JButton("Ausstehende Aufgaben");
        showFinished = new JButton("Erledigte Aufgaben");
        panelMid.add(sync);
        panelMid.add(showPending);
        panelMid.add(showFinished);

        // Listener für Buttons zum Ansicht wechseln
        showPending.addActionListener(showPendingListener);

        showFinished.addActionListener(showFinishedListener);

        // Panel für Buttons unten
        JPanel panelDown = new JPanel();
        panelDown.setLayout(new BoxLayout(panelDown, BoxLayout.Y_AXIS));
        newTask = new JButton("Neue Aufgabe");
        newTask.addActionListener(newTaskListener);
        panelDown.add(newTask);


        // Links mainPanel
        mainPanelLeft = new JPanel();
        mainPanelLeft.setLayout(new GridLayout(3,0,0,0));
        mainPanelLeft.setBackground(Color.lightGray);

        mainPanelLeft.add(panelUp);
        mainPanelLeft.add(panelMid);
        mainPanelLeft.add(panelDown);



        // Liste und ScrollPane für anzeige der Tasks
        // DefaultListModel mit Titeln und Datum dazu füllen
        titles = new DefaultListModel<>();
        ArrayList<Task> pendingTasks;
        try {
            pendingTasks = db.getTasks(TaskState.Pending, SortOrder.Ascending);
        } catch (DatabaseException e) {
            Log.e("Main", "error", e);
            return;
        }
        for (Task t : pendingTasks) {
            String str = t.title().get();
            int len = str.length();
            int temp = 60 - len;
            for (int i = 0; i < temp; ++i) {
                str = str + " ";
            }
            LocalDateTime date = t.dueDate().get();
            str = str + date.getDayOfMonth() + "." + date.getMonthValue() + "." + date.getYear();
            titles.addElement(str);
        }

        // JList erstellen
        JList<String> listPending = new JList<>(titles);
        listPending.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        listPending.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        scrollPaneTasks = new JScrollPane(listPending);


        // Panel rechts im Bild
        mainPanelRight = new JPanel();
        mainPanelRight.setLayout(new BoxLayout(mainPanelRight, BoxLayout.Y_AXIS));
        JLabel mainPanelRightLabel = new JLabel("Ausstehende Aufgaben");
        mainPanelRight.add(mainPanelRightLabel);
        mainPanelRight.add(scrollPaneTasks);


        // Panels zu Frame hinzufügen und Frame sichtbar machen
        this.add(mainPanelLeft, BorderLayout.WEST);
        this.add(mainPanelRight, BorderLayout.CENTER);


        this.getContentPane().revalidate();
        this.getContentPane().repaint();
    }

    public void showPending() {
        titles.clear();
        ArrayList<Task> pendingTasks;
        try {
            pendingTasks = db.getTasks(TaskState.Pending, SortOrder.Ascending);
        } catch (DatabaseException e) {
            Log.e("Main", "error", e);
            return;
        }
        pendingTasks.forEach((Task t) -> {
            String str = t.title().get();
            int len = str.length();
            int temp = 60 - len;
            for (int i = 0; i < temp; ++i) {
                str = str + " ";
            }
            LocalDateTime date = t.dueDate().get();
            str = str + date.getDayOfMonth() + "." + date.getMonthValue() + "." + date.getYear();
            titles.addElement(str);});


        // JList erstellen
        JList<String> listPending = new JList<>(titles);
        listPending.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        listPending.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        scrollPaneTasks = new JScrollPane(listPending);


        mainPanelRight.removeAll();
        JLabel mainPanelRightLabel = new JLabel("Ausstehende Aufgaben");
        mainPanelRight.add(mainPanelRightLabel);
        mainPanelRight.add(scrollPaneTasks);


        mainPanelRight.updateUI();
        scrollPaneTasks.updateUI();




        scrollPaneTasks.updateUI();
    }

    public void showFinished() {
        titles.clear();
        ArrayList<Task> pendingTasks;
        try {
             pendingTasks = db.getTasks(TaskState.Finished, SortOrder.Descending);
        } catch (DatabaseException e) {
            Log.e("Main", "error", e);
            return;
        }
        pendingTasks.forEach((Task t) -> {titles.addElement(t.title().get());});

        // JList erstellen
        JList<String> listFinished = new JList<>(titles);
        listFinished.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        listFinished.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        scrollPaneTasks = new JScrollPane(listFinished);


        mainPanelRight.removeAll();
        JLabel mainPanelRightLabel = new JLabel("Erledigte Aufgaben");
        mainPanelRight.add(mainPanelRightLabel);
        mainPanelRight.add(scrollPaneTasks);


        mainPanelRight.updateUI();
        scrollPaneTasks.updateUI();
    }


    public void showAddTask() {
        this.getContentPane().removeAll();


        // Hauptpanel erzeugen
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Überschrift und Textfield für Titel
        JLabel newTask = new JLabel("Neue Aufgabe anlegen");
        mainPanel.add(newTask);


        // Panel für Titel
        JPanel panelOne = new JPanel();
        panelOne.setLayout(new BoxLayout(panelOne, BoxLayout.X_AXIS));
        panelOne.add(new JLabel("Titel:"));
        JTextField textfieldTitle = new JTextField();
        panelOne.add(textfieldTitle);
        mainPanel.add(panelOne);




        // Panel für Priorität
        JPanel panel = new JPanel();
        String[] priorityList = {"niedrig", "mittel", "hoch"};
        JComboBox<String> priorityBox = new JComboBox<>(priorityList);
        panel.add(new JLabel("Priorität"), BorderLayout.WEST);
        panel.add(priorityBox, BorderLayout.EAST);
        mainPanel.add(panel);


        // Panel für Fälligkeitsdatum
        JPanel panelTwo = new JPanel();
        panelTwo.setLayout(new BoxLayout(panelTwo, BoxLayout.X_AXIS));
        panelTwo.add(new JLabel("Fälligkeitsdatum:"));
        String placeholder = "dd.mm.yyyy hh:mm";
        JTextField textfieldDueDate = new JTextField(placeholder);
        textfieldDueDate.setForeground(Color.GRAY);
        textfieldDueDate.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textfieldDueDate.getText().equals("dd.mm.yyyy hh:mm"))
                    textfieldDueDate.setText("");


            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textfieldDueDate.getText().equals(""))
                    textfieldDueDate.setText(placeholder);

            }
        });

        panelTwo.add(textfieldDueDate);
        mainPanel.add(panelTwo);

        // TextArea für Beschreibung
        mainPanel.add(new JLabel("Beschreibung:"));
        JTextArea descriptionArea = new JTextArea(15, 30);
        JScrollPane descriptionAreaScrollPane = new JScrollPane(descriptionArea);



        // Buttons für speichern und abbrechen
        JButton save = new JButton("speichern");
        JButton cancel = new JButton("abbrechen");
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(0,2,0,0));
        buttonPanel.add(save);
        buttonPanel.add(cancel);



        // Listener für Buttons
        ActionListener saveListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Prüfen, dass Title nicht leer und nicht zu lang ist
                String title = textfieldTitle.getText();
                if (title.length() == 0) {
                    JOptionPane.showMessageDialog(null, "Ungültige Eingabe:\nDer Titel darf nicht leer sein.");
                    return;
                } else if (title.length() > 50) {
                    JOptionPane.showMessageDialog(null, "Ungültige Eingabe:\nDer Titel darf höchstens 50 Zeichen lang sein.\nAktuell: " + title.length() + " Zeichen.");
                    return;
                }

                // Prüfen, dass Fälligkeitsdatum nicht leer oder ungültig ist oder in der Vergangenheit liegt
                String dueDateString = textfieldDueDate.getText();
                LocalDateTime dueDate = checkDueDate(dueDateString);
                if (dueDate == null)
                    return;

                // Priorität auslesen
                String selectedPriority = (String) priorityBox.getSelectedItem();

                // Beschreibung auslesen
                String description = descriptionArea.getText();

                // Neuen Task in db speichern
                try {
                    if (selectedPriority.equals("niedrig")) {
                        db.upsertTask(Task.create(title, description, TaskState.Pending, TaskPriority.Low, dueDate));
                        setOverview();
                        return;
                    }
                    if (selectedPriority.equals("mittel")) {
                        db.upsertTask(Task.create(title, description, TaskState.Pending, TaskPriority.Mid, dueDate));
                        setOverview();
                        return;
                    }
                    if (selectedPriority.equals("hoch")) {
                        db.upsertTask(Task.create(title, description, TaskState.Pending, TaskPriority.High, dueDate));
                        setOverview();
                        return;
                    }
                } catch (DatabaseException dbEx) {
                    JOptionPane.showMessageDialog(null, "Datenbankfehler\nDie Aufgabe konnte nicht gespeichert werden.\nBitte versuchen sie es erneut.");
                    setOverview();
                    return;
                }
            }
        };
        save.addActionListener(saveListener);

        ActionListener cancelListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setOverview();
            }
        };
        cancel.addActionListener(cancelListener);



        // Einzelene Panels zum gesamten Layout zusammenfügen
        this.add(mainPanel, BorderLayout.NORTH);
        this.add(descriptionAreaScrollPane, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);

        this.getContentPane().revalidate();
        this.getContentPane().repaint();
    }



    public LocalDateTime checkDueDate(String dueDateString) {
        // Prüfen ob keine Eingabe erfolgt ist
        if (dueDateString.equals("dd.mm.yyyy hh:mm")) {
            JOptionPane.showMessageDialog(null, "Ungültige Eingabe:\nDas Fälligkeitsdatum darf nicht leer sein.");
            return null;
        }

        String[] dueDateArr = dueDateString.split("[.: ]");
        if (dueDateArr.length != 5) {
            JOptionPane.showMessageDialog(null, "Ungültige Eingabe:\nDas Fälligkeitsdatum hat ein ungültiges Format.");
            return null;
        }


        int day = 0;
        int month = 0;
        int year = 0;
        int hour = 0;
        int minute = 0;


        try {
            day = Integer.parseInt(dueDateArr[0]);
            month = Integer.parseInt(dueDateArr[1]);
            year = Integer.parseInt(dueDateArr[2]);
            hour = Integer.parseInt(dueDateArr[3]);
            minute = Integer.parseInt(dueDateArr[4]);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Ungültige Eingabe:\nDas Fälligkeitsdatum hat ein ungültiges Format.");
            return null;
        }

        LocalDateTime dueDate;
        try {
            dueDate = LocalDateTime.of(year, month, day, hour, minute);
        } catch (DateTimeException dateEx) {
            JOptionPane.showMessageDialog(null, "Ungültige Eingabe:\nDie übergebenen Zahlen stellen kein gültiges Datum dar.");
            return null;
        }

        return dueDate;
    }



}
