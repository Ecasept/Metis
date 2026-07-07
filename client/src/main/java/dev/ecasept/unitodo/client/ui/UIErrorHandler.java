package dev.ecasept.unitodo.client.ui;

import dev.ecasept.unitodo.client.api.exception.ApiException;
import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.utils.Log;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.util.concurrent.CompletionException;

public class UIErrorHandler {

    /**
     * Handles errors from async operations by giving user feedback
     *
     * @param t          The exception thrown by the CompletableFuture.
     * @param actionName The name of the action (e.g., "Löschen des Accounts").
     * @param errorTitle The title for the JOptionPane (e.g., "Account löschen fehlgeschlagen").
     */
    public static void handleAsyncError(Throwable t, String actionName, String errorTitle) {
        Throwable cause = (t instanceof CompletionException) ? t.getCause() : t;
        String message;

        // Determine the specific error message
        if (cause instanceof ApiException c) {
            message = "Netzwerk Fehler beim " + actionName + ". Bitte versuchen Sie es erneut. Fehler: " + c.getErrorCode().getMessage();
        } else if (cause instanceof DatabaseException) {
            message = "Datenbankfehler beim " + actionName + ". Bitte versuchen Sie es erneut.";
        } else {
            message = "Unbekannter Fehler beim " + actionName + ". Bitte versuchen Sie es erneut.";
        }

        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, message, errorTitle, JOptionPane.ERROR_MESSAGE);
        });
    }
}
