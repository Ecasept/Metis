package dev.ecasept.unitodo.client;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.function.Consumer;

public class AJSearchbar extends JPanel {
    public AJSearchbar(Consumer<String> callback) {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        String placeholder = "Suchen...";

        JTextField input = new JTextField(placeholder);

        input.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                callback.accept(input.getText());
            }
            @Override public void insertUpdate(DocumentEvent e) { update(); }
            @Override public void removeUpdate(DocumentEvent e) { update(); }
            @Override public void changedUpdate(DocumentEvent e) { }
        });

        input.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (input.getText().equals(placeholder))
                    input.setText("");
                callback.accept(input.getText());
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (input.getText().isEmpty())
                    input.setText(placeholder);
            }
        });

        input.setSize(100, 20);

        this.add(input);
    }
}
