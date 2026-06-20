CREATE TABLE IF NOT EXISTS tasks
(
    uuid               TEXT    NOT NULL UNIQUE PRIMARY KEY,
    title              TEXT    NOT NULL,
    description        TEXT    NOT NULL,
    state              INTEGER NOT NULL,
    priority           INTEGER NOT NULL,
    dueDate            INTEGER NOT NULL,
    dueTime            INTEGER NOT NULL,

    titleChanged       INTEGER NOT NULL,
    descriptionChanged INTEGER NOT NULL,
    stateChanged       INTEGER NOT NULL,
    priorityChanged    INTEGER NOT NULL,
    dueDateChanged     INTEGER NOT NULL,
    dueTimeChanged     INTEGER NOT NULL,

    completedAt        INTEGER,

    isDeleted          TEXT    NOT NULL,
    deletedChanged     INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS variables(
    key TEXT NOT NULL UNIQUE PRIMARY KEY,
    value TEXT NOT NULL
);
