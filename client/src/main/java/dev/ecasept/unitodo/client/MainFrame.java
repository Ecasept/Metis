package dev.ecasept.unitodo.client;

import dev.ecasept.unitodo.client.db.DatabaseRepository;
import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.db.SortOrder;
import dev.ecasept.unitodo.shared.models.db.Task;
import dev.ecasept.unitodo.shared.models.db.TaskPriority;
import dev.ecasept.unitodo.shared.models.db.TaskState;
import dev.ecasept.unitodo.shared.utils.Log;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.metal.MetalIconFactory;
import java.awt.*;
import java.awt.event.*;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

@SuppressWarnings("LanguageDetectionInspection")
public class MainFrame extends JFrame {

    private final DatabaseRepository db;

    // Aktuelle Ansicht (Pending oder Finished)
    private static final int LAST_WAS_FINISHED = 1;
    private static final int LAST_WAS_PENDING = 2;
    private int last;

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
    private ActionListener syncListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("SYNCHRONISATION");
        }
    };







    public MainFrame(DatabaseRepository db) {
        this.db = db;
        last = LAST_WAS_PENDING;

        // Frame vorbereiten
        this.setTitle("To-Do Liste");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800, 450);
        this.setBackground(Color.lightGray);
        this.setLocationRelativeTo(null);

        setOverview();
        LoginFrame test = new LoginFrame(this, true);
        // JOptionPane.showInputDialog("Bitte Benutzername und Passwort eingeben:");

        this.setVisible(true);
    }

    public void setOverview() {
        this.getContentPane().removeAll();

        // Menüleiste hinzufügen
        JMenuBar mainMenuBar = new JMenuBar();
        // Account Menü
        JMenu accountMenu = new JMenu("Account");
        JMenuItem logoutMenuItem = new JMenuItem("Abmelden");
        JMenuItem deleteAccountMenuItem = new JMenuItem("Account löschen");
        accountMenu.add(logoutMenuItem);
        accountMenu.add(deleteAccountMenuItem);
        mainMenuBar.add(accountMenu);

        // Ansicht Menü
        JMenu viewMenu = new JMenu("Ansicht");
        JMenuItem pendingTasksItem = new JMenuItem("Ausstehende Aufgaben");
        JMenuItem finishedTasksItem = new JMenuItem("Erledigte Aufgabe");
        viewMenu.add(pendingTasksItem);
        viewMenu.add(finishedTasksItem);
        mainMenuBar.add(viewMenu);

        // Button, um zu aynchronisieren
        JButton syncMenuItem = new JButton("Sync");
        mainMenuBar.add(syncMenuItem);

        // Button, um neuen Task anzulegen
        JButton newTaskItem = new JButton("Neue Aufgabe");
        mainMenuBar.add(newTaskItem);

        this.add(mainMenuBar, BorderLayout.NORTH);


        // Listener für Menü
        pendingTasksItem.addActionListener(showPendingListener);
        finishedTasksItem.addActionListener(showFinishedListener);
        newTaskItem.addActionListener(newTaskListener);
        syncMenuItem.addActionListener(syncListener);





        // Liste und ScrollPane für anzeige der Tasks
        // DefaultListModel mit Titeln und Datum dazu füllen
        titles = new DefaultListModel<>();
        ArrayList<Task> pendingTasks;
        if (last == LAST_WAS_PENDING) {
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
        } else {
            try {
                pendingTasks = db.getTasks(TaskState.Finished, SortOrder.Descending);
            } catch (DatabaseException e) {
                Log.e("Main", "error", e);
                return;
            }
            pendingTasks.forEach((Task t) -> {titles.addElement(t.title().get());});
        }

        // JList erstellen
        JList<String> listPending = new JList<>(titles);
        listPending.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        listPending.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listPending.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedIndex = listPending.getSelectedIndex();
                    Task selectedTask = pendingTasks.get(selectedIndex);
                    showChangeTask(selectedTask.uuid());
                }
            }
        });


        scrollPaneTasks = new JScrollPane(listPending);


        // Panel rechts im Bild
        mainPanelRight = new JPanel();
        mainPanelRight.setLayout(new BoxLayout(mainPanelRight, BoxLayout.Y_AXIS));
        JLabel mainPanelRightLabel;
        if (last == LAST_WAS_PENDING) {
            mainPanelRightLabel = new JLabel("Ausstehende Aufgaben");
        } else {
            mainPanelRightLabel = new JLabel("Erledigte Aufgaben");
        }
        mainPanelRight.add(mainPanelRightLabel);
        mainPanelRight.add(scrollPaneTasks);


        // Panels zu Frame hinzufügen und Frame sichtbar machen
        this.add(mainPanelRight, BorderLayout.CENTER);


        this.getContentPane().revalidate();
        this.getContentPane().repaint();
    }

    public void showPending() {
        // Variable für letzte Seite auf Pending stellen
        last = LAST_WAS_PENDING;


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
            titles.addElement(str);
        });


        // JList erstellen
        JList<String> listPending = new JList<>(titles);
        listPending.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        listPending.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listPending.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedIndex = listPending.getSelectedIndex();
                    Task selectedTask = pendingTasks.get(selectedIndex);
                    showChangeTask(selectedTask.uuid());
                }
            }
        });

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
        // Variable für letzte Seite auf Finished stellen
        last = LAST_WAS_FINISHED;

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
        listFinished.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedIndex = listFinished.getSelectedIndex();
                    Task selectedTask = pendingTasks.get(selectedIndex);
                    showChangeTask(selectedTask.uuid());
                }
            }
        });

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
            JOptionPane.showMessageDialog(null, "Ungültige Eingabe:\nDie eingegebenen Zahlen stellen kein gültiges Datum dar.");
            return null;
        }

        if (dueDate.isBefore(LocalDateTime.now())) {
            JOptionPane.showMessageDialog(null, "Ungültige Eingabe:\nDas gewählt Fälligkeitsdatum liegt in der Vergangenhet.");
            return null;
        }

        return dueDate;
    }


    public void showChangeTask(UUID uuid) {
        this.getContentPane().removeAll();

        // Taskobjekt aus DB auslesen
        Task task;
        try {
            task = this.db.getTask(uuid.toString()).get();
        } catch (DatabaseException e) {
            Log.e("Main", "error", e);
            return;
        }


        // Hauptpanel erzeugen
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Buttons für Löschen und TaskState ändern
        JButton changeTaskStateButton = new JButton();
        if (task.state().get().equals(TaskState.Pending))
            changeTaskStateButton.setText("Erledigt");
        if (task.state().get().equals(TaskState.Finished))
            changeTaskStateButton.setText("Ausstehend");

        changeTaskStateButton.setBackground(new Color(0, 150, 0));

        JButton deleteTaskButton = new JButton("Löschen");
        deleteTaskButton.setBackground(new Color(230, 0, 0));
        JPanel upperButtonsPanel = new JPanel();
        upperButtonsPanel.setLayout(new GridLayout(1,2));
        upperButtonsPanel.add(changeTaskStateButton);
        upperButtonsPanel.add(deleteTaskButton);
        mainPanel.add(upperButtonsPanel);


        // Listener für Buttons zum Löschen und TaskState ändern
        changeTaskStateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                    if (task.state().get().equals(TaskState.Pending)) {
                        task.state().set(TaskState.Finished);
                        try {
                            db.upsertTask(task);
                        } catch (DatabaseException ex) {
                            Log.e("Main", "error", ex);
                            return;
                        }
                    } else if (task.state().get().equals(TaskState.Finished)) {
                        task.state().set(TaskState.Pending);
                        try {
                            db.upsertTask(task);
                        } catch (DatabaseException ex) {
                            Log.e("Main", "error", ex);
                            return;
                        }
                    }

                    setOverview();

            }
        });

        deleteTaskButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int x = JOptionPane.showConfirmDialog(null, "Möchten sie die Aufgabe wirklich löschen?");
                    if (x == JOptionPane.YES_OPTION) {
                        db.deleteTask(uuid.toString());
                    } else {
                        return;
                    }
                } catch (DatabaseException ex) {
                    Log.e("Main", "error", ex);
                    return;
                }

                setOverview();
            }
        });



        // Überschrift und Textfield für Titel
        JLabel newTask = new JLabel("Aufgabe bearbeiten");
        mainPanel.add(newTask);


        // Panel für Titel
        JPanel panelOne = new JPanel();
        panelOne.setLayout(new BoxLayout(panelOne, BoxLayout.X_AXIS));
        panelOne.add(new JLabel("Titel:"));
        JTextField textfieldTitle = new JTextField();
        textfieldTitle.setText(task.title().get());
        panelOne.add(textfieldTitle);
        mainPanel.add(panelOne);




        // Panel für Priorität und TaskState
        JPanel panel = new JPanel(new GridLayout(1,2));
        String[] priorityList = {"niedrig", "mittel", "hoch"};
        JComboBox<String> priorityBox = new JComboBox<>(priorityList);
        if (task.priority().get().equals(TaskPriority.Low)) {
            priorityBox.setSelectedIndex(0);
        } else if (task.priority().get().equals(TaskPriority.Mid)) {
            priorityBox.setSelectedIndex(1);
        } else if (task.priority().get().equals(TaskPriority.High)) {
            priorityBox.setSelectedIndex(2);
        }
        JPanel subPanel1 = new JPanel(new FlowLayout());
        subPanel1.add(new JLabel("Priorität"));
        subPanel1.add(priorityBox);
        panel.add(subPanel1);
        JLabel taskStateLabel = new JLabel("Status:");
        JTextField taskStateField = new JTextField();
        if (task.state().get().equals(TaskState.Pending)) {
            taskStateField.setText("Ausstehend");
        }
        if (task.state().get().equals(TaskState.Finished)) {
            taskStateField.setText("Erledigt");
        }

        taskStateField.setEditable(false);
        JPanel subPanel2 = new JPanel();
        subPanel2.add(taskStateLabel);
        subPanel2.add(taskStateField);
        panel.add(subPanel2);
        mainPanel.add(panel);


        // Panel für Fälligkeitsdatum
        JPanel panelTwo = new JPanel();
        panelTwo.setLayout(new BoxLayout(panelTwo, BoxLayout.X_AXIS));
        panelTwo.add(new JLabel("Fälligkeitsdatum:"));
        String placeholder = task.dueDate().get().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
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
                    textfieldDueDate.setText("dd.mm.yyyy hh:mm");

            }
        });

        panelTwo.add(textfieldDueDate);
        mainPanel.add(panelTwo);

        // TextArea für Beschreibung
        mainPanel.add(new JLabel("Beschreibung:"));
        JTextArea descriptionArea = new JTextArea(15, 30);
        descriptionArea.setText(task.description().get());
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
                // Wenn sich neuer Titel von altem unterscheidet, dann neuen Titel setzen
                if (!title.equals(task.title().get())) {
                    task.title().set(title);
                }


                // Prüfen, dass Fälligkeitsdatum nicht leer oder ungültig ist oder in der Vergangenheit liegt
                String dueDateString = textfieldDueDate.getText();
                LocalDateTime dueDate = checkDueDate(dueDateString);
                if (dueDate == null)
                    return;
                // Wenn sich neues Fälligkeitsdatum von altem unterscheidet, dann neues Fälligkeitsdatum setzen
                if (!dueDate.equals(task.dueDate().get())) {
                    task.dueDate().set(dueDate);
                }

                // Priorität auslesen
                String selectedPriority = (String) priorityBox.getSelectedItem();
                // Wenn sich neue Priorität von alter unterscheidet, dann neue Priorität setzen
                if (!selectedPriority.equals(task.priority().get().toString())) {
                    if (selectedPriority.equals("niedrig")) {
                        task.priority().set(TaskPriority.Low);
                    }
                    if (selectedPriority.equals("mittel")) {
                        task.priority().set(TaskPriority.Mid);
                    }
                    if (selectedPriority.equals("hoch")) {
                        task.priority().set(TaskPriority.High);
                    }
                }

                // Beschreibung auslesen
                String description = descriptionArea.getText();
                // Wenn sich neue Beschreibung von alter unterscheidet, dann neue Beschreibung setzen
                if (!description.equals(task.description().get())) {
                    task.description().set(description);
                }

                // Änderungen in db speichern
                try {
                    db.upsertTask(task);
                    setOverview();
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


}
