package dev.ecasept.unitodo.client.ui.dialog;

import dev.ecasept.unitodo.client.DataManager;
import dev.ecasept.unitodo.client.api.exception.ApiException;
import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.models.api.Password;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.concurrent.CompletionException;

public class RegisterDialog extends JDialog {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private DataManager dataManager;


    public RegisterDialog(Frame x, boolean modal, DataManager dataManager) {
        super(x, "Registrieren", modal);
        this.dataManager = dataManager;
        this.setLocation(550, 320);

        JPanel usernamePanel = new JPanel();
        JLabel usernameLabel = new JLabel("Benutzername:");
        usernameField = new JTextField();
        usernamePanel.setLayout(new BoxLayout(usernamePanel, BoxLayout.X_AXIS));
        usernamePanel.add(usernameLabel);
        usernamePanel.add(usernameField);

        JPanel passwordPanel = new JPanel();
        JLabel passwordLabel = new JLabel("Passwort: ");
        passwordField = new JPasswordField();
        passwordPanel.setLayout(new BoxLayout(passwordPanel, BoxLayout.X_AXIS));
        passwordPanel.add(passwordLabel);
        passwordPanel.add(passwordField);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.add(usernamePanel);
        inputPanel.add(passwordPanel);

        JButton registerButton = new JButton("Registrieren");
        JButton cancelButton = new JButton("Abbrechen");
        registerButton.addActionListener(registerButtonListener);
        cancelButton.addActionListener(cancelButtonListener);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);



        this.add(inputPanel, BorderLayout.NORTH);
        this.add(buttonPanel, BorderLayout.CENTER);


        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.pack();
        this.setVisible(true);
    }


    ActionListener registerButtonListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Prüfung ob username und password leer sind
            String username = usernameField.getText();
            if (username.equals("")) {
                JOptionPane.showMessageDialog(null, "Der Benutzername darf nicht leer sein.", "Registrierung fehgeschlagen", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Password password;
            try {
                password = new Password(passwordField.getPassword());
            } catch (NullPointerException ex) {
                JOptionPane.showMessageDialog(null, "Das Passwort darf nicht leer sein.", "Registrierung fehgeschlagen", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (password.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Das Passwort darf nicht leer sein.", "Registrierung fehgeschlagen", JOptionPane.ERROR_MESSAGE);
                password.shred();
                return;
            }

            dataManager.register(username, password).exceptionally(t -> {
                 var cause = (t instanceof CompletionException) ? t.getCause() : t;
                 if (cause instanceof ApiException) {
                     JOptionPane.showMessageDialog(null, "Fehler beim Registrieren. Bitte versuchen sie es erneut", "Registrierung fehlgeschlagen", JOptionPane.ERROR_MESSAGE);
                 } else if (cause instanceof DatabaseException) {
                     JOptionPane.showMessageDialog(null, "Datenbankfehler bei der Registrierung. Bitte versuchen sie es erneut", "Registrierung fehlgeschlagen", JOptionPane.ERROR_MESSAGE);
                 } else {
                     JOptionPane.showMessageDialog(null, "Unbekannter Fehler bei der Registrierung. Bitte versuchen sie es erneut", "Registrierung fehlgeschlagen", JOptionPane.ERROR_MESSAGE);
                 }
                 return null;
            }).whenComplete((r, t) -> {
                password.shred();
            });
        }
    };

    ActionListener cancelButtonListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            dispose();
        }
    };
}

