package dev.ecasept.unitodo.client.ui.utils;

/**
 * This interface provives an abstract method used for a callback to the MainFrame when the
 * sync with the server is finished.
 */
@FunctionalInterface
public interface SyncResponse {

    void syncFinished();
}
