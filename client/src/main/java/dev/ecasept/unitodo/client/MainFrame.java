package dev.ecasept.unitodo.client;

import dev.ecasept.unitodo.models.Task;
import dev.ecasept.unitodo.models.TaskManager;
import dev.ecasept.unitodo.models.TaskState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.util.ArrayList;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;

public class MainFrame extends JFrame {

    private TaskManager manager;
    // Lambda-Ausdrücke zum Filtern und sortieren der Tasks
    private Predicate<Task> taskPending = (Task t) -> {return t.getState() == TaskState.Pending;
    };
    private Predicate<Task> taskFinished = (Task t) -> {return t.getState() == TaskState.Finished;
    };
    private Comparator<Task> sortNextDueDate =
            (Task a, Task b) -> {
        Optional<LocalDateTime> OpdateA = a.getDueDate();
        Optional<LocalDateTime> OpdateB = b.getDueDate();
        LocalDateTime dateA = OpdateA.get();
        LocalDateTime dateB = OpdateB.get();
        if (dateA.isBefore(dateB)){return -1;}
        if (dateA.isAfter(dateB)){return 1;}
        if (dateA.isEqual(dateB)){return 0;}
        return 0;
    };

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




    public MainFrame(TaskManager manager) {
        this.manager = manager;

        setOverview();

        this.setVisible(true);
    }

    public void setOverview() {
        this.getContentPane().removeAll();

        // Frame vorbereiten
        this.setTitle("To-Do Liste");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800, 450);
        this.setBackground(Color.lightGray);



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
        ArrayList<Task> pendingTasks = manager.getFilteredSortedTasks(taskPending, sortNextDueDate);
        for (Task t : pendingTasks) {
            String str = t.getName();
            int len = str.length();
            int temp = 60 - len;
            for (int i = 0; i < temp; ++i) {
                str = str + " ";
            }
            Optional<LocalDateTime> Opdate = t.getDueDate();
            LocalDateTime date = Opdate.get();
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
        ArrayList<Task> pendingTasks = manager.getFilteredSortedTasks(taskPending, sortNextDueDate);
        pendingTasks.forEach((Task t) -> {
            String str = t.getName();
            int len = str.length();
            int temp = 60 - len;
            for (int i = 0; i < temp; ++i) {
                str = str + " ";
            }
            Optional<LocalDateTime> Opdate = t.getDueDate();
            LocalDateTime date = Opdate.get();
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
        ArrayList<Task> pendingTasks = manager.getFilteredSortedTasks(taskFinished, sortNextDueDate);
        pendingTasks.forEach((Task t) -> {titles.addElement(t.getName());});

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


        // Panel für Fälligkeitsdatum
        JPanel panelOne = new JPanel();
        panelOne.setLayout(new BoxLayout(panelOne, BoxLayout.X_AXIS));
        panelOne.add(new JLabel("Titel:"));
        JTextField textfield = new JTextField();
        panelOne.add(textfield);
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
        JTextField textfieldTitle = new JTextField();
        panelTwo.add(textfieldTitle);
        mainPanel.add(panelTwo);

        // TextArea für Beschreibung
        mainPanel.add(new JLabel("Beschreibung"));
        JTextArea descriptionArea = new JTextArea();
        mainPanel.add(descriptionArea);


        // Buttons für speichern und abbrechen
        JButton save = new JButton("speichern");
        JButton cancel = new JButton("abbrechen");
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(0,2,0,0));
        buttonPanel.add(save);
        buttonPanel.add(cancel);
        this.add(buttonPanel, BorderLayout.SOUTH);


        // Listener für Buttons
        ActionListener saveListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Save!!!");
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




        this.add(mainPanel, BorderLayout.NORTH);
        this.getContentPane().revalidate();
        this.getContentPane().repaint();
    }
}
