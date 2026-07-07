package dev.ecasept.unitodo.client.ui.dialog;

import dev.ecasept.unitodo.client.DataManager;
import dev.ecasept.unitodo.client.api.exception.ApiException;
import dev.ecasept.unitodo.client.ui.UIErrorHandler;
import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.models.api.Password;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.concurrent.CompletionException;

/**
 * This class is responsible for creating a (modal) dialog box where the user can log in with their account.
 */
public class LoginDialog extends JDialog {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private DataManager dataManager;

    /**
     * Creates an new dialog box where the user can log in with their account.
     *
     * @param x the parent frame.
     * @param modal true if the dialog should be modal, false otherwise.
     * @param dataManager the DataManager-Object which manages the account and login status.
     */
    public LoginDialog(Frame x, boolean modal, DataManager dataManager) {
        super(x, "Anmelden", modal);
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

        JButton loginButton = new JButton("Anmelden");
        JButton cancelButton = new JButton("Abbrechen");
        loginButton.addActionListener(loginButtonListener);
        cancelButton.addActionListener(cancelButtonListener);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(loginButton);
        buttonPanel.add(cancelButton);



        this.add(inputPanel, BorderLayout.NORTH);
        this.add(buttonPanel, BorderLayout.CENTER);


        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.pack();
        this.setVisible(true);
    }


    private ActionListener loginButtonListener = new ActionListener() {
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

            // Abfrage ob lokale Daten verworfen werden sollen oder mit dem Server zusammengeführt werden sollen
            int discardLocalChangesOption = JOptionPane.showConfirmDialog(null, "Möchten sie die lokalen Daten und die des Servers zusammenführen?\n(Andernfalls wird der lokale Datenstand verworfen und der des Servers hergestellt)", "Synchronisation", JOptionPane.YES_NO_OPTION);
            boolean discardLocalChanges;
            if (discardLocalChangesOption == JOptionPane.YES_OPTION) {
                discardLocalChanges = false;
            } else {
                discardLocalChanges = true;
            }
            dataManager.login(username, password)
                .whenComplete((r, t) -> {
                    password.shred();

                    if (t != null) {
                        UIErrorHandler.handleAsyncError(t, "Anmelden", "Anmeldung fehlgeschlagen");
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            dispose();
                        });
                    }
                });
        }
    };

    private ActionListener cancelButtonListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            dispose();
        }
    };
}
