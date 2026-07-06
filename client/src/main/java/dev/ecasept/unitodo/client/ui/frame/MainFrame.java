package dev.ecasept.unitodo.client.ui.frame;

import dev.ecasept.unitodo.client.*;
import dev.ecasept.unitodo.client.ui.component.AJSearchbar;
import dev.ecasept.unitodo.client.ui.component.TaskTableEditor;
import dev.ecasept.unitodo.client.ui.dialog.DeleteAccountDialog;
import dev.ecasept.unitodo.client.ui.dialog.LoginDialog;
import dev.ecasept.unitodo.client.ui.dialog.RegisterDialog;
import dev.ecasept.unitodo.client.ui.utils.FrameUtils;
import dev.ecasept.unitodo.client.ui.utils.SyncResponse;
import dev.ecasept.unitodo.client.ui.utils.TimeUtils;
import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.models.db.ClientTask;
import dev.ecasept.unitodo.shared.models.db.TaskPriority;
import dev.ecasept.unitodo.shared.models.db.TaskState;
import dev.ecasept.unitodo.shared.utils.Log;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

/**
 * This class is the central GUI class of the program. It is responsible for displaying the
 * JTable containing the users taks and the JMenuBar with menu buttons for account management, switching
 * between pending and finished tasks, creating new tasks and the searchbar.
 * In Addition this class manages the user interactions with all buttons in the JMenuBar.
 */

public class MainFrame extends JFrame {
    private static final String TAG = "MainFrame";

    private final DataManager dataManger;

    // Aktuelle Ansicht (Pending oder Finished)
    private static final int LAST_WAS_FINISHED = 1;
    private static final int LAST_WAS_PENDING = 2;
    private static final int LAST_WAS_ALL = 3;
    private static final int LAST_WAS_SEARCHED = 4;
    private static String lastSearchString;
    private int last;

    // Anmeldestatus + Buttons zur Accountverwaltung
    private boolean loggedIn = false;
    JMenuItem logInOutMenuItem;
    JMenuItem registerDeleteAccountMenuItem;

    // Elemente der GUI
    private JScrollPane scrollPaneTasks;
    private DefaultListModel<String> titles;
    JPanel mainPanelLeft;
    JPanel mainPanelRight;

    // Lables für Übersicht
    JLabel mainPanelRightLabel;

    // Zentrale JTable für die dargestellten Tasks
    private JTable taskTable;
    private DefaultTableModel tableModel;
    private String[] rows = {"Status", "Titel", "Fälligkeitsdatum", "Priorität", "", ""};
    private ArrayList<ClientTask> currentlyShownTasks;


    // SyncResponse, um setOverview nach sync aufzurufen
    SyncResponse syncResponse = new SyncResponse() {
        @Override
        public void syncFinished() {
            if (last == LAST_WAS_ALL) {
                showAll();
            } else if (last == LAST_WAS_FINISHED){
                showFinished();
            } else if (last == LAST_WAS_PENDING){
                showPending();
            } else {
                showSearched(lastSearchString);
            }
        }
    };


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
            if (!loggedIn) {
                JOptionPane.showMessageDialog(null, "Synchronisation nicht möglich. Bitte melden sie sich an.", "Synchronisation nicht möglich", JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                dataManger.synchronize();
            }
        }
    };
    private ActionListener logInOutMenuItemListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
           if (logInOutMenuItem.getActionCommand().equals("Anmelden")) {

               LoginDialog loginFrame = new LoginDialog(null, true, dataManger);
               try {
                   loggedIn = dataManger.isLoggedIn();
               } catch (DatabaseException ex) {
                   JOptionPane.showMessageDialog(null, "Datenbankfehler! Die Daten konnten nicht korrekt geladen werden", "Datenbankfehler", JOptionPane.ERROR_MESSAGE);
                   return;
               }
               setOverview();

           } else if (logInOutMenuItem.getActionCommand().equals("Abmelden")) {
               try {
                   dataManger.logout();
                   loggedIn = dataManger.isLoggedIn();
               } catch (DatabaseException ex) {
                   JOptionPane.showMessageDialog(null, "Abmelden nicht möglich. Bitte versuchen sie es erneut.", "Abmelden nicht möglich", JOptionPane.ERROR_MESSAGE);
                   return;
               }
               setOverview();
           }
        }
    };

    private ActionListener registerDeleteAccountMenuItemListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("Registrieren")) {
                RegisterDialog registerDialog = new RegisterDialog(null, true, dataManger);
                try {
                    loggedIn = dataManger.isLoggedIn();
                } catch (DatabaseException ex) {
                    JOptionPane.showMessageDialog(null, "Datenbankfehler! Die Daten konnten nicht korrekt geladen werden", "Datenbankfehler", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else if (e.getActionCommand().equals("Account löschen")) {
                DeleteAccountDialog deleteAccountDialog = new DeleteAccountDialog(null, true, dataManger);
                try {
                    loggedIn = dataManger.isLoggedIn();
                } catch (DatabaseException ex) {
                    JOptionPane.showMessageDialog(null, "Datenbankfehler! Die Daten konnten nicht korrekt geladen werden", "Datenbankfehler", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }
    };


    /**
     * Creates a new MainFrame-Object
     * @param dataManger the DataManager-Object that manages the login status and the users tasks.
     */
    public MainFrame(DataManager dataManger) {
        this.dataManger = dataManger;
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

        this.setVisible(true);
    }

    /**
     * This methods sets the task overview and the JMenuBar. It is called at the programs start
     * and always when the user returns from one of the views to add, edit or show a task.
     */
    public void setOverview() {
        this.getContentPane().removeAll();

        // Menüleiste hinzufügen
        JMenuBar mainMenuBar = new JMenuBar();
        // Account Menü
        JMenu accountMenu = new JMenu("Account");
        if (!loggedIn) {
            logInOutMenuItem = new JMenuItem("Anmelden");
            registerDeleteAccountMenuItem = new JMenuItem("Registrieren");
        } else {
            logInOutMenuItem = new JMenuItem("Abmelden");
            registerDeleteAccountMenuItem = new JMenuItem("Account löschen");
        }
        accountMenu.add(logInOutMenuItem);
        accountMenu.add(registerDeleteAccountMenuItem);
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
        logInOutMenuItem.addActionListener(logInOutMenuItemListener);
        registerDeleteAccountMenuItem.addActionListener(registerDeleteAccountMenuItemListener);





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
        } else if (last == LAST_WAS_ALL){
            currentlyShownTasks = FrameUtils.fillListAndTableModelAll(tableModel, dataManger);
        } else if (last == LAST_WAS_SEARCHED){
            currentlyShownTasks = FrameUtils.fillListAndTableModelSearched(tableModel, dataManger, lastSearchString);
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

    /**
     * Sets the JTable displaying the users tasks to show all tasks regardless of their state.
     */
    public void showAll() {
        last = LAST_WAS_ALL;

        if (taskTable.isEditing()) {
            taskTable.getCellEditor().stopCellEditing();
        }

        tableModel.setRowCount(0);
        currentlyShownTasks = FrameUtils.fillListAndTableModelAll(tableModel, dataManger);
        mainPanelRightLabel.setText("Alle Aufgaben");
    }

    /**
     * Sets the JTable displaying only taks
     * based on what the user searched for in the search bar.
     */
    public void showSearched(String searchString) {
        last = LAST_WAS_SEARCHED;
        lastSearchString = searchString;

        if (taskTable.isEditing()) {
            taskTable.getCellEditor().stopCellEditing();
        }

        tableModel.setRowCount(0);
        currentlyShownTasks = FrameUtils.fillListAndTableModelSearched(tableModel, dataManger, searchString);
        mainPanelRightLabel.setText("Suchergebnisse: " + searchString);
    }

    /**
     * Sets the JTable displaying the users tasks to show
     * only taks that are pending.
     */
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

    /**
     * Sets the JTable displaying the users tasks to show
     * only taks that are finished.
     */
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

    /**
     * Shows the view where the user can add a new task.
     */
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
        String placeholderDate = "dd.mm.yyyy";
        String placeholderTime = "hh:mm";
        JTextField textfieldDueDate = new JTextField(placeholderDate);
        JTextField textfieldDueTime = new JTextField((placeholderTime));
        textfieldDueDate.setForeground(Color.GRAY);
        textfieldDueDate.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textfieldDueDate.getText().equals("dd.mm.yyyy"))
                    textfieldDueDate.setText("");


            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textfieldDueDate.getText().equals(""))
                    textfieldDueDate.setText(placeholderDate);

            }
        });

        textfieldDueTime.setForeground(Color.GRAY);
        textfieldDueTime.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textfieldDueTime.getText().equals("hh:mm"))
                    textfieldDueTime.setText("");


            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textfieldDueTime.getText().equals(""))
                    textfieldDueTime.setText(placeholderTime);

            }
        });

        panelTwo.add(textfieldDueDate);
        panelTwo.add(new JLabel("Uhrzeit:"));
        panelTwo.add(textfieldDueTime);
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
                LocalDate dueDate = TimeUtils.checkDueDate(dueDateString);
                if (dueDate == null)
                    return;

                // Prüfen, dass die Fälligkeitsuhrzeit nicht leer oder ungültig ist
                String dueTimeString = textfieldDueTime.getText();
                LocalTime dueTime = null;
                if (!(dueTimeString.equals("hh:mm") || dueTimeString.equals(""))) {
                    try {
                        dueTime = TimeUtils.checkDueTime(dueTimeString, dueDate);
                    } catch (IllegalArgumentException ex) {
                        return;
                    }
                }


                // Priorität auslesen
                String selectedPriority = (String) priorityBox.getSelectedItem();

                // Beschreibung auslesen
                String description = descriptionArea.getText();

                // Neuen Task in db speichern
                try {
                    if (selectedPriority.equals("niedrig")) {
                        dataManger.upsertTask(ClientTask.create(title, description, new TaskState.Pending(), TaskPriority.Low, dueDate, Optional.ofNullable(dueTime)));
                        setOverview();
                        return;
                    }
                    if (selectedPriority.equals("mittel")) {
                        dataManger.upsertTask(ClientTask.create(title, description, new TaskState.Pending(), TaskPriority.Mid, dueDate, Optional.ofNullable(dueTime)));
                        setOverview();
                        return;
                    }
                    if (selectedPriority.equals("hoch")) {
                        dataManger.upsertTask(ClientTask.create(title, description, new TaskState.Pending(), TaskPriority.High, dueDate, Optional.ofNullable(dueTime)));
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


    /**
     * Shows the view where the user can add a new task.
     * @param uuid the unique uuid of the task the user wants to edit.
     */
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
        textfieldTitle.setText(task.getTitle());
        panelOne.add(textfieldTitle);
        mainPanel.add(panelOne);




        // Panel für Priorität und TaskState
        JPanel panel = new JPanel(new GridLayout(1,2));
        String[] priorityList = {"niedrig", "mittel", "hoch"};
        JComboBox<String> priorityBox = new JComboBox<>(priorityList);
        if (task.getPriority().equals(TaskPriority.Low)) {
            priorityBox.setSelectedIndex(0);
        } else if (task.getPriority().equals(TaskPriority.Mid)) {
            priorityBox.setSelectedIndex(1);
        } else if (task.getPriority().equals(TaskPriority.High)) {
            priorityBox.setSelectedIndex(2);
        }
        JPanel subPanel1 = new JPanel(new FlowLayout());
        subPanel1.add(new JLabel("Priorität"));
        subPanel1.add(priorityBox);
        panel.add(subPanel1);
        JLabel taskStateLabel = new JLabel("Status:");
        JTextField taskStateField = new JTextField();
        if (task.getState().isPending()) {
            taskStateField.setText("Ausstehend");
        }
        if (task.getState().isFinished()) {
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
        String placeholderDate = task.getDueDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        String placeholderTime = "hh:mm";
        if (task.getDueTime().isPresent()) {
            placeholderTime = task.getDueTime().get().format(DateTimeFormatter.ofPattern("HH:mm"));
        }

        JTextField textfieldDueDate = new JTextField(placeholderDate);
        JTextField textfieldDueTime = new JTextField((placeholderTime));
        textfieldDueDate.setForeground(Color.GRAY);
        textfieldDueDate.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textfieldDueDate.getText().equals("dd.mm.yyyy"))
                    textfieldDueDate.setText("");


            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textfieldDueDate.getText().equals(""))
                    textfieldDueDate.setText(placeholderDate);

            }
        });

        textfieldDueTime.setForeground(Color.GRAY);
        textfieldDueTime.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textfieldDueTime.getText().equals("hh:mm"))
                    textfieldDueTime.setText("");


            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textfieldDueTime.getText().equals("")) {
                    textfieldDueTime.setText("hh:mm");
                }

            }
        });

        panelTwo.add(textfieldDueDate);
        panelTwo.add(new JLabel("Uhrzeit:"));
        panelTwo.add(textfieldDueTime);
        mainPanel.add(panelTwo);















        // TextArea für Beschreibung
        JPanel middlePanel = new JPanel();
        middlePanel.add(new JLabel("Beschreibung:"));
        mainPanel.add(middlePanel);
        JTextArea descriptionArea = new JTextArea(15, 30);
        descriptionArea.setText(task.getDescription());
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
                var t = task;
                // Prüfen, dass Title nicht leer und nicht zu lang ist
                String title = textfieldTitle.getText();
                if (title.length() == 0) {
                    JOptionPane.showMessageDialog(null, "Ungültige Eingabe:\nDer Titel darf nicht leer sein.");
                    return;
                } else if (title.length() > 50) {
                    JOptionPane.showMessageDialog(null, "Ungültige Eingabe:\nDer Titel darf höchstens 50 Zeichen lang sein.\nAktuell: " + title.length() + " Zeichen.");
                    return;
                }
                t = t.withTitle(title);

                // Prüfen, dass Fälligkeitsdatum nicht leer oder ungültig ist oder in der Vergangenheit liegt
                String dueDateString = textfieldDueDate.getText();
                String dueTimeString = textfieldDueTime.getText();
                LocalDate dueDate = TimeUtils.checkDueDate(dueDateString);
                if (dueDate == null)
                    return;

                LocalTime dueTime = null;
                if (!(dueTimeString.equals("hh:mm") || dueTimeString.equals(""))) {
                    try {
                        dueTime = TimeUtils.checkDueTime(dueTimeString, dueDate);
                    } catch (IllegalArgumentException ex) {
                        return;
                    }
                }

                t = t.withDueDate(dueDate).withDueTime(Optional.ofNullable(dueTime));

                // Priorität auslesen
                String selectedPriority = (String) priorityBox.getSelectedItem();
                switch (selectedPriority) {
                    case "niedrig" -> t = t.withPriority(TaskPriority.Low);
                    case "mittel" -> t = t.withPriority(TaskPriority.Mid);
                    case "hoch" -> t = t.withPriority(TaskPriority.High);
                }

                // Beschreibung auslesen
                String description = descriptionArea.getText();
                t = t.withDescription(description);

                // Änderungen in db speichern
                try {
                    dataManger.upsertTask(t);
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

    /**
     * Displays all information about a task without allowing the user to edit it.
     * @param uuid the unique uuid of the task the user wants to display.
     */
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
        textfieldTitle.setText(task.getTitle());
        textfieldTitle.setEditable(false);
        panelOne.add(textfieldTitle);
        mainPanel.add(panelOne);




        // Panel für Priorität und TaskState
        JPanel panel = new JPanel(new GridLayout(1,2));
        String[] priorityList = {"niedrig", "mittel", "hoch"};
        JTextField priorityField = new JTextField();
        if (task.getPriority().equals(TaskPriority.Low)) {
            priorityField.setText("niedrig");
        } else if (task.getPriority().equals(TaskPriority.Mid)) {
            priorityField.setText("mittel");
        } else if (task.getPriority().equals(TaskPriority.High)) {
            priorityField.setText("hoch");
        }
        priorityField.setEditable(false);
        JPanel subPanel1 = new JPanel(new FlowLayout());
        subPanel1.add(new JLabel("Priorität"));
        subPanel1.add(priorityField);
        panel.add(subPanel1);
        JLabel taskStateLabel = new JLabel("Status:");
        JTextField taskStateField = new JTextField();
        if (task.getState().isPending()) {
            taskStateField.setText("Ausstehend");
        }
        if (task.getState().isFinished()) {
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
        String placeholderDate = task.getDueDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        String placeholderTime = "hh:mm";
        if (task.getDueTime().isPresent()) {
            placeholderTime = task.getDueTime().get().format(DateTimeFormatter.ofPattern("HH:mm"));
        }

        JTextField textfieldDueDate = new JTextField(placeholderDate);
        JTextField textfieldDueTime = new JTextField((placeholderTime));
        textfieldDueDate.setEditable(false);
        textfieldDueTime.setEditable(false);
        textfieldDueDate.setForeground(Color.GRAY);

        panelTwo.add(textfieldDueDate);
        panelTwo.add(new JLabel("Uhrzeit:"));
        panelTwo.add(textfieldDueTime);
        mainPanel.add(panelTwo);










        // TextArea für Beschreibung
        JPanel middlePanel = new JPanel();
        middlePanel.add(new JLabel("Beschreibung:"));
        mainPanel.add(middlePanel);
        JTextArea descriptionArea = new JTextArea(15, 30);
        descriptionArea.setText(task.getDescription());
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


    /**
     * This method is called by the JTable whenever the user clicks on the edit-icon of a task.
     * @param row the row in the JTable the user clicked the edit-icon for
     */
    public void editTaskClicked(int row) {
        if (!currentlyShownTasks.isEmpty()) {
            UUID uuid = currentlyShownTasks.get(row).uuid();
            showChangeTask(uuid);
        }
    }

    /**
     * This method is called by the JTable whenever the user clicks on the delete-icon of a task.
     * @param row the row in the JTable the user clicked the delete-icon for
     */
    public void deleteTaskClicked(int row) {
        Log.i(TAG, "Löschen von Zeile " + row);

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
            } else if (last == LAST_WAS_FINISHED){
                showFinished();
            } else {
                showSearched(lastSearchString);
            }
        }
    }

    /**
     * This method is called by the JTable whenever the user clicks on the checkbox of a task.
     * @param row the row in the JTable the user clicked the checkbox for.
     */
    public void changeStateTaskClicked(int row) {
       Log.i(TAG, "Status ändern von Zeile " + row);

       if (!currentlyShownTasks.isEmpty()) {
           ClientTask task = currentlyShownTasks.get(row);


           if (task.getState().isPending()) {
               try {
                   dataManger.upsertTask(task.withState(new TaskState.Finished(LocalDateTime.now())));
                   if (last == LAST_WAS_ALL) {
                       showAll();
                   } else if (last == LAST_WAS_FINISHED){
                       showFinished();
                   } else if (last == LAST_WAS_PENDING){
                       showPending();
                   } else {
                       showSearched(lastSearchString);
                   }
               } catch (DatabaseException ex) {
                   Log.e("Main", "error", ex);
                   JOptionPane.showMessageDialog(this, "Datenbankfehler! Der Status der Aufgabe konnte nicht geändert werden. Bitte versuchen sei es erneut.", "Datenbankfehler", JOptionPane.ERROR_MESSAGE);
                   return;
               }
           } else if (task.getState().isFinished()) {
               try {
                   dataManger.upsertTask(task.withState(new TaskState.Pending()));
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

    /**
     * This method is called by the JTable whenever the user clicks on the title of a taks.
     * @param row the row in the JTable the user clicked task-title for.
     */
    public void showTaskClicked(int row) {
        if (!currentlyShownTasks.isEmpty()) {
            UUID uuid = currentlyShownTasks.get(row).uuid();
            showOnlyTask(uuid);
        }
    }

}
