package dev.ecasept.unitodo.client.ui.component;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.function.Consumer;

/**
 * A search bar from AJ, you have a text input and the shown tasks are filtered by the input. (name and description of the task)
 */
public class AJSearchbar extends JPanel {
    private boolean shouldUpdate = true;

    /**
     * Creates a new AJSearchbar with a callback that is called when the input changes.
     * @param callback A callback with String parameter for search input.
     */
    public AJSearchbar(Consumer<String> callback) {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        String placeholder = "Suchen...";

        JTextField input = new JTextField(placeholder);

        input.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                if (shouldUpdate) {
                    callback.accept(input.getText());
                }
            }
            @Override public void insertUpdate(DocumentEvent e) { update(); }
            @Override public void removeUpdate(DocumentEvent e) { update(); }
            @Override public void changedUpdate(DocumentEvent e) { }
        });

        input.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (input.getText().equals(placeholder)) {
                    input.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (input.getText().isEmpty()) {
                    shouldUpdate = false;
                    input.setText(placeholder);
                    shouldUpdate = true;
                }
            }
        });

        input.setSize(100, 20);

        this.add(input);
    }
}
