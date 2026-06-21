package dev.ecasept.unitodo.client;

import dev.ecasept.unitodo.client.api.exception.ApiException;
import dev.ecasept.unitodo.shared.db.DatabaseException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

public class DeleteAccountDialog extends JDialog {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private DataManager dataManager;


    public DeleteAccountDialog(Frame x, boolean modal, DataManager dataManager) {
        super(x, "Account löschen", modal);
        this.dataManager = dataManager;
        this.setLocation(550, 320);

        JPanel usernamePanel = new JPanel();
        JLabel usernameLabel = new JLabel("Bitte Passwort eingeben");
        usernamePanel.setLayout(new BoxLayout(usernamePanel, BoxLayout.X_AXIS));
        usernamePanel.add(usernameLabel);


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

        JButton deleteButton = new JButton("Account löschen");
        JButton cancelButton = new JButton("Abbrechen");
        deleteButton.addActionListener(deleteButtonListener);
        cancelButton.addActionListener(cancelButtonListener);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(deleteButton);
        buttonPanel.add(cancelButton);



        this.add(inputPanel, BorderLayout.NORTH);
        this.add(buttonPanel, BorderLayout.CENTER);


        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.pack();
        this.setVisible(true);
    }


    ActionListener deleteButtonListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Prüfung password leer ist
            char[] passwordArray;
            try {
                passwordArray = passwordField.getPassword();
            } catch (NullPointerException ex) {
                JOptionPane.showMessageDialog(null, "Das Passwort darf nicht leer sein.", "Registrierung fehgeschlagen", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (passwordArray.length == 0) {
                JOptionPane.showMessageDialog(null, "Das Passwort darf nicht leer sein.", "Registrierung fehgeschlagen", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Account löschen
            try {
                dataManager.deleteAccount(Arrays.toString(passwordArray));
                Arrays.fill(passwordArray, '0');
            } catch (ApiException eA) {
                JOptionPane.showMessageDialog(null, "Fehler löschen des Accounts. Bitte versuchen sie es erneut", "Account löschen fehlgeschlagen", JOptionPane.ERROR_MESSAGE);
                Arrays.fill(passwordArray, '0');
                return;
            } catch (DatabaseException eD) {
                JOptionPane.showMessageDialog(null, "Datenbankfehler. Bitte versuchen sie es erneut", "Account löschen fehlgeschlagen", JOptionPane.ERROR_MESSAGE);
                Arrays.fill(passwordArray, '0');
                return;
            }

        }
    };

    ActionListener cancelButtonListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            dispose();
        }
    };

}
