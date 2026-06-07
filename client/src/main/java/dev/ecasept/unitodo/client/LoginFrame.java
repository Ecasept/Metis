package dev.ecasept.unitodo.client;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JDialog {

    public LoginFrame(Frame x, boolean modal) {
        super(x, "Anmelden", false);
        this.setLocation(780, 450);

        JPanel usernamePanel = new JPanel();
        JLabel usernameLabel = new JLabel("Benutzername:");
        JTextField usernameField = new JTextField();
        usernamePanel.setLayout(new BoxLayout(usernamePanel, BoxLayout.X_AXIS));
        usernamePanel.add(usernameLabel);
        usernamePanel.add(usernameField);

        JPanel passwordPanel = new JPanel();
        JLabel passwordLabel = new JLabel("Passwort: ");
        JPasswordField passwordField = new JPasswordField();
        passwordPanel.setLayout(new BoxLayout(passwordPanel, BoxLayout.X_AXIS));
        passwordPanel.add(passwordLabel);
        passwordPanel.add(passwordField);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.add(usernamePanel);
        inputPanel.add(passwordPanel);

        JButton okButton = new JButton("Ok");
        JButton cancelButton = new JButton("Abbrechen");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        JLabel notYetRegisteredLabel = new JLabel("Noch nicht registriert?");
        JButton registerButton = new JButton("Registrieren");
        JPanel registerPanel = new JPanel();
        registerPanel.add(notYetRegisteredLabel);
        registerPanel.add(registerButton);


        this.add(inputPanel, BorderLayout.NORTH);
        this.add(buttonPanel, BorderLayout.CENTER);
        this.add(registerPanel, BorderLayout.SOUTH);


        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setSize(350, 180);
        this.setVisible(true);
    }
}
