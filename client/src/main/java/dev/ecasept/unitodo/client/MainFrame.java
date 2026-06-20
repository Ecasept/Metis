package dev.ecasept.unitodo.client;

import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.models.db.ClientTask;
import dev.ecasept.unitodo.shared.models.db.TaskPriority;
import dev.ecasept.unitodo.shared.models.db.TaskState;
import dev.ecasept.unitodo.shared.utils.Log;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.UUID;

@SuppressWarnings("LanguageDetectionInspection")
public class MainFrame extends JFrame {
    private static final String TAG = "MainFrame";

    private final DataManager dataManger;

    // Aktuelle Ansicht (Pending oder Finished)
    private static final int LAST_WAS_FINISHED = 1;
    private static final int LAST_WAS_PENDING = 2;
    private static final int LAST_WAS_ALL = 3;
    private int last;

    // Anmeldestatus + Buttons zur Accountverwaltung
    private boolean loggedIn = false;
    JMenuItem logInOutMenuItem;
    JMenuItem registerDeleteMenuAccountMenuItem;

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

    // Lables für Übersicht
    JLabel mainPanelRightLabel;

    // Zentrale JTable für die dargestellten Tasks
    private JTable taskTable;
    private DefaultTableModel tableModel;
    private String[] rows = {"Status", "Titel", "Fälligkeitsdatum", "Priorität", "", ""};
    private ArrayList<ClientTask> currentlyShownTasks;

    // Listener für die Button
    private ActionListener showAllListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            showAll();
        }
    };
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







    public MainFrame(DataManager dataManger) {
        this.dataManger = dataManger;
        dataManger.setAsyncErrorHandler(
                e -> {
                    JOptionPane.showMessageDialog(this, "Datenbankfehler! Die Daten konnten nicht korrekt synchronisiert werden", "Datenbankfehler", JOptionPane.ERROR_MESSAGE);
                    Log.e(TAG, "Error during asynchronous database operation", e);
                }
        );
        try {
            dataManger.initialize();
            loggedIn = dataManger.isLoggedIn();
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, "Datenbankfehler! Die Daten konnten nicht korrekt geladen werden", "Datenbankfehler", JOptionPane.ERROR_MESSAGE);
            return;
        }


        last = LAST_WAS_ALL;

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
        if (!loggedIn) {
            logInOutMenuItem = new JMenuItem("Anmelden");
            registerDeleteMenuAccountMenuItem = new JMenuItem("Registrieren");
        } else {
            logInOutMenuItem = new JMenuItem("Abmelden");
            registerDeleteMenuAccountMenuItem = new JMenuItem("Account löschen");
        }
        accountMenu.add(logInOutMenuItem);
        accountMenu.add(registerDeleteMenuAccountMenuItem);
        mainMenuBar.add(accountMenu);

        // Ansicht Menü
        JMenu viewMenu = new JMenu("Ansicht");
        JMenuItem allTasksItem = new JMenuItem("Alle Aufgaben");
        JMenuItem pendingTasksItem = new JMenuItem("Ausstehende Aufgaben");
        JMenuItem finishedTasksItem = new JMenuItem("Erledigte Aufgaben");
        viewMenu.add(allTasksItem);
        viewMenu.add(pendingTasksItem);
        viewMenu.add(finishedTasksItem);
        mainMenuBar.add(viewMenu);

        // Button, um zu aynchronisieren
        JButton syncMenuItem = new JButton("Sync");
        mainMenuBar.add(syncMenuItem);

        // Button, um neuen Task anzulegen
        JButton newTaskItem = new JButton("Neue Aufgabe");
        mainMenuBar.add(newTaskItem);

        AJSearchbar searchbar = new AJSearchbar(this::showSearched);
        mainMenuBar.add(searchbar);

        this.add(mainMenuBar, BorderLayout.NORTH);


        // Listener für Menü
        allTasksItem.addActionListener(showAllListener);
        pendingTasksItem.addActionListener(showPendingListener);
        finishedTasksItem.addActionListener(showFinishedListener);
        newTaskItem.addActionListener(newTaskListener);
        syncMenuItem.addActionListener(syncListener);





        // JTable und ScrollPane für anzeige der Tasks
        // DefaultTableModel mit Titeln und Datum dazu füllen
        tableModel = new DefaultTableModel(rows, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 2 && column != 3;
            }
        };


        if (last == LAST_WAS_PENDING) {
            currentlyShownTasks = FrameUtils.fillListAndTableModelPending(tableModel, dataManger);
        } else if (last == LAST_WAS_FINISHED) {
            currentlyShownTasks = FrameUtils.fillListAndTableModelFinished(tableModel, dataManger);
        } else {
            currentlyShownTasks = FrameUtils.fillListAndTableModelAll(tableModel, dataManger);
        }

        // JTable erstellen und in ScrollPane einbetten
        taskTable = FrameUtils.getConfiguredTable(tableModel);
        taskTable.setDefaultEditor(Object.class, new TaskTableEditor(this));
        scrollPaneTasks = new JScrollPane(taskTable);










        // Panel rechts im Bild
        mainPanelRight = new JPanel();
        mainPanelRight.setLayout(new BoxLayout(mainPanelRight, BoxLayout.Y_AXIS));
        JPanel upperPanel = new JPanel();
        upperPanel.setLayout(new BorderLayout());
        if (last == LAST_WAS_PENDING) {
            mainPanelRightLabel = new JLabel("Ausstehende Aufgaben");
        } else if (last == LAST_WAS_FINISHED) {
            mainPanelRightLabel = new JLabel("Erledigte Aufgaben");
        } else {
            mainPanelRightLabel = new JLabel("Alle Aufgaben");
        }
        mainPanelRightLabel.setFont(new Font("Arial", Font.BOLD, 15));
        mainPanelRightLabel.setHorizontalAlignment(SwingConstants.CENTER);
        upperPanel.add(mainPanelRightLabel, BorderLayout.CENTER);
        mainPanelRight.add(upperPanel);
        mainPanelRight.add(scrollPaneTasks);


        // Panels zu Frame hinzufügen und Frame sichtbar machen
        this.add(mainPanelRight, BorderLayout.CENTER);


        this.getContentPane().revalidate();
        this.getContentPane().repaint();
    }

    public void showAll() {
        last = LAST_WAS_ALL;

        if (taskTable.isEditing()) {
            taskTable.getCellEditor().stopCellEditing();
        }

        tableModel.setRowCount(0);
        currentlyShownTasks = FrameUtils.fillListAndTableModelAll(tableModel, dataManger);
        mainPanelRightLabel.setText("Alle Aufgaben");
    }

    public void showSearched(String searchString) {
        if (taskTable.isEditing()) {
            taskTable.getCellEditor().stopCellEditing();
        }

        tableModel.setRowCount(0);
        currentlyShownTasks = FrameUtils.fillListAndTableModelSearched(tableModel, dataManger, searchString);
        mainPanelRightLabel.setText("Suchergebnisse: " + searchString);
    }

    public void showPending() {
        // Variable für letzte Seite auf Pending stellen
        last = LAST_WAS_PENDING;

        if (taskTable.isEditing()) {
            taskTable.getCellEditor().stopCellEditing(); // oder .cancelCellEditing();
        }

        tableModel.setRowCount(0);



        currentlyShownTasks = FrameUtils.fillListAndTableModelPending(tableModel, dataManger);

        mainPanelRightLabel.setText("Ausstehende Aufgaben");
    }

    public void showFinished() {
        // Variable für letzte Seite auf Finished stellen
        last = LAST_WAS_FINISHED;

        if (taskTable.isEditing()) {
            taskTable.getCellEditor().stopCellEditing(); // oder .cancelCellEditing();
        }

        tableModel.setRowCount(0);


        currentlyShownTasks = FrameUtils.fillListAndTableModelFinished(tableModel, dataManger);
        mainPanelRightLabel.setText("Erledigte Aufgaben");
    }


    public void showAddTask() {
        this.getContentPane().removeAll();


        // Hauptpanel erzeugen
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Überschrift und Textfield für Titel
        JLabel newTask = new JLabel("Neue Aufgabe anlegen");
        newTask.setHorizontalAlignment(SwingConstants.CENTER);
        newTask.setFont(new Font("Arial", Font.BOLD, 15));
        JPanel upperPanel = new JPanel();
        upperPanel.setLayout(new BorderLayout());
        upperPanel.add(newTask, BorderLayout.CENTER);
        mainPanel.add(upperPanel);


        // Panel für Titel
        JPanel panelOne = new JPanel();
        panelOne.setLayout(new BorderLayout());
        JLabel titleLabel = new JLabel("Titel:");
        panelOne.add(titleLabel, BorderLayout.WEST);
        JTextField textfieldTitle = new JTextField();
        panelOne.add(textfieldTitle, BorderLayout.CENTER);
        mainPanel.add(panelOne);




        // Panel für Priorität
        JPanel panel = new JPanel();
        String[] priorityList = {"niedrig", "mittel", "hoch"};
        JComboBox<String> priorityBox = new JComboBox<>(priorityList);
        panel.add(new JLabel("Priorität:"), BorderLayout.WEST);
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
        JPanel middlePanel = new JPanel();
        middlePanel.add(new JLabel("Beschreibung:"));
        mainPanel.add(middlePanel);
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
                        dataManger.upsertTask(ClientTask.create(title, description, TaskState.Pending, TaskPriority.Low, dueDate));
                        setOverview();
                        return;
                    }
                    if (selectedPriority.equals("mittel")) {
                        dataManger.upsertTask(ClientTask.create(title, description, TaskState.Pending, TaskPriority.Mid, dueDate));
                        setOverview();
                        return;
                    }
                    if (selectedPriority.equals("hoch")) {
                        dataManger.upsertTask(ClientTask.create(title, description, TaskState.Pending, TaskPriority.High, dueDate));
                        setOverview();
                        return;
                    }
                } catch (DatabaseException dbEx) {
                    JOptionPane.showMessageDialog(null, "Datenbankfehler\nDie Aufgabe konnte nicht gespeichert werden.\nBitte versuchen sie es erneut.", "Datenbankfehler", JOptionPane.ERROR_MESSAGE);
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
        ClientTask task;
        try {
            task = this.dataManger.getTask(uuid.toString()).get();
        } catch (DatabaseException e) {
            Log.e("Main", "error", e);
            return;
        }


        // Hauptpanel erzeugen
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));





        // Überschrift und Textfield für Titel
        JLabel newTask = new JLabel("Aufgabe bearbeiten");
        newTask.setHorizontalAlignment(SwingConstants.CENTER);
        newTask.setFont(new Font("Arial", Font.BOLD, 15));
        JPanel upperPanel = new JPanel();
        upperPanel.setLayout(new BorderLayout());
        upperPanel.add(newTask, BorderLayout.CENTER);
        mainPanel.add(upperPanel);



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
        JPanel middlePanel = new JPanel();
        middlePanel.add(new JLabel("Beschreibung:"));
        mainPanel.add(middlePanel);
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
                    dataManger.upsertTask(task);
                    setOverview();
                } catch (DatabaseException dbEx) {
                    JOptionPane.showMessageDialog(null, "Datenbankfehler\nDie Aufgabe konnte nicht gespeichert werden.\nBitte versuchen sie es erneut.", "Datenbankfehler", JOptionPane.ERROR_MESSAGE);
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


    public void showOnlyTask(UUID uuid) {
        this.getContentPane().removeAll();

        // Taskobjekt aus DB auslesen
        ClientTask task;
        try {
            task = this.dataManger.getTask(uuid.toString()).get();
        } catch (DatabaseException e) {
            Log.e("Main", "error", e);
            return;
        }


        // Hauptpanel erzeugen
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));





        // Überschrift und Textfield für Titel
        JLabel newTask = new JLabel("Aufgabe anzeigen");
        newTask.setHorizontalAlignment(SwingConstants.CENTER);
        newTask.setFont(new Font("Arial", Font.BOLD, 15));
        JPanel upperPanel = new JPanel();
        upperPanel.setLayout(new BorderLayout());
        upperPanel.add(newTask, BorderLayout.CENTER);
        mainPanel.add(upperPanel);



        // Panel für Titel
        JPanel panelOne = new JPanel();
        panelOne.setLayout(new BoxLayout(panelOne, BoxLayout.X_AXIS));
        panelOne.add(new JLabel("Titel:"));
        JTextField textfieldTitle = new JTextField();
        textfieldTitle.setText(task.title().get());
        textfieldTitle.setEditable(false);
        panelOne.add(textfieldTitle);
        mainPanel.add(panelOne);




        // Panel für Priorität und TaskState
        JPanel panel = new JPanel(new GridLayout(1,2));
        String[] priorityList = {"niedrig", "mittel", "hoch"};
        JTextField priorityField = new JTextField();
        if (task.priority().get().equals(TaskPriority.Low)) {
            priorityField.setText("niedrig");
        } else if (task.priority().get().equals(TaskPriority.Mid)) {
            priorityField.setText("mittel");
        } else if (task.priority().get().equals(TaskPriority.High)) {
            priorityField.setText("hoch");
        }
        priorityField.setEditable(false);
        JPanel subPanel1 = new JPanel(new FlowLayout());
        subPanel1.add(new JLabel("Priorität"));
        subPanel1.add(priorityField);
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
        textfieldDueDate.setEditable(false);
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
        JPanel middlePanel = new JPanel();
        middlePanel.add(new JLabel("Beschreibung:"));
        mainPanel.add(middlePanel);
        JTextArea descriptionArea = new JTextArea(15, 30);
        descriptionArea.setText(task.description().get());
        descriptionArea.setEditable(false);
        JScrollPane descriptionAreaScrollPane = new JScrollPane(descriptionArea);



        // Buttons für speichern und abbrechen
        JButton okButton = new JButton("Ok");
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(0,1,0,0));
        buttonPanel.add(okButton);
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setOverview();
            }
        });






        // Einzelene Panels zum gesamten Layout zusammenfügen
        this.add(mainPanel, BorderLayout.NORTH);
        this.add(descriptionAreaScrollPane, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);

        this.getContentPane().revalidate();
        this.getContentPane().repaint();
    }


    // Methoden für Benutzeraktionen in der JTable die die Tasks anzeigt
    public void editTaskClicked(int row) {
        if (!currentlyShownTasks.isEmpty()) {
            UUID uuid = currentlyShownTasks.get(row).uuid();
            showChangeTask(uuid);
        }
    }

    public void deleteTaskClicked(int row) {
        System.out.println("Löschen von Zeile " + row);

        if (!currentlyShownTasks.isEmpty()) {
            ClientTask deleteThis  = currentlyShownTasks.get(row);


            try {
                int x = JOptionPane.showConfirmDialog(null, "Möchten sie die Aufgabe wirklich löschen?");
                if (x == JOptionPane.YES_OPTION) {
                    dataManger.deleteTask(deleteThis);
                    tableModel.removeRow(row);
                    currentlyShownTasks.remove(row);
                } else {
                    return;
                }
            } catch (DatabaseException ex) {
                JOptionPane.showMessageDialog(this, "Datenbankfehler! Die Aufgabe konnte nicht gelöscht werden. Bitte versuchen sei es erneut.", "Datenbankfehler", JOptionPane.ERROR_MESSAGE);
                Log.e("Main", "error", ex);
                return;
            }

            if (last == LAST_WAS_ALL) {
                showAll();
            } else if (last == LAST_WAS_PENDING) {
                showPending();
            } else {
                showFinished();
            }
        }
    }

    public void changeStateTaskClicked(int row) {
       System.out.println("Status ändern von Zeile " + row);

       if (!currentlyShownTasks.isEmpty()) {
           ClientTask task = currentlyShownTasks.get(row);


           if (task.state().get().equals(TaskState.Pending)) {
               task.state().set(TaskState.Finished);
               try {
                   dataManger.upsertTask(task);
                   if (last == LAST_WAS_ALL) {
                       showAll();
                   } else if (last == LAST_WAS_FINISHED){
                       showFinished();
                   } else {
                       showPending();
                   }
               } catch (DatabaseException ex) {
                   Log.e("Main", "error", ex);
                   JOptionPane.showMessageDialog(this, "Datenbankfehler! Der Status der Aufgabe konnte nicht geändert werden. Bitte versuchen sei es erneut.", "Datenbankfehler", JOptionPane.ERROR_MESSAGE);
                   return;
               }
           } else if (task.state().get().equals(TaskState.Finished)) {
               task.state().set(TaskState.Pending);
               try {
                   dataManger.upsertTask(task);
                   if (last == LAST_WAS_ALL) {
                       showAll();
                   } else {
                       showFinished();
                   }
               } catch (DatabaseException ex) {
                   Log.e("Main", "error", ex);
                   JOptionPane.showMessageDialog(this, "Datenbankfehler! Der Status der Aufgabe konnte nicht geändert werden. Bitte versuchen sei es erneut.", "Datenbankfehler", JOptionPane.ERROR_MESSAGE);
                   return;
               }
           }
       }

    }

    public void showTaskClicked(int row) {
        if (!currentlyShownTasks.isEmpty()) {
            UUID uuid = currentlyShownTasks.get(row).uuid();
            showOnlyTask(uuid);
        }
    }

}
