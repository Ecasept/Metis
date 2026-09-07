package dev.ecasept.unitodo.client.ui;

import dev.ecasept.unitodo.client.api.exception.ApiException;
import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.models.api.ErrorCode;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class UIErrorHandler {
    private final AtomicBoolean reloginPromptOpen = new AtomicBoolean();
    private final Consumer<Component> onSessionExpired;

    public UIErrorHandler(Consumer<Component> onSessionExpired) {
        this.onSessionExpired = onSessionExpired;
    }

    /** Handles async operation errors on the Swing event thread. */
    public void handleAsyncError(Throwable t, String actionName, String errorTitle,
                                        Component parent) {
        Throwable cause = t;
        while (cause instanceof CompletionException && cause.getCause() != null) {
            cause = cause.getCause();
        }
        if (cause instanceof ApiException api && api.getErrorCode() == ErrorCode.AUTH_TOKEN_EXPIRED) {
            // Claim the prompt before queuing it so concurrent failures show only one dialog.
            if (reloginPromptOpen.compareAndSet(false, true)) {
                SwingUtilities.invokeLater(() -> {
                    try {
                        onSessionExpired.accept(parent);
                    } finally {
                        reloginPromptOpen.set(false);
                    }
                });
            }
            return;
        }
        String message;
        if (cause instanceof ApiException c) {
            message = "Netzwerk Fehler beim " + actionName + ". Bitte versuchen Sie es erneut. Fehler: " + c.getErrorCode().getMessage();
        } else if (cause instanceof DatabaseException) {
            message = "Datenbankfehler beim " + actionName + ". Bitte versuchen Sie es erneut.";
        } else {
            message = "Unbekannter Fehler beim " + actionName + ". Bitte versuchen Sie es erneut.";
        }
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(parent, message, errorTitle, JOptionPane.ERROR_MESSAGE));
    }

}
