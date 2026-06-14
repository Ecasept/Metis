package dev.ecasept.unitodo.client.sync;

public enum SyncState {
    /** The client has local changes in the db */
    NeedsFullSync,
    /** The client has local changes in the db and in memory that it can directly sync */
    Dirty,
    /** The client is fully synced up */
    Synced,
}
