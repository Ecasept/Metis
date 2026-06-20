CREATE TABLE IF NOT EXISTS tasks (
    uuid TEXT NOT NULL UNIQUE,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    state INTEGER NOT NULL,
    priority INTEGER NOT NULL,
    dueDate INTEGER NOT NULL,
    dueTime INTEGER NOT NULL,

    titleChanged INTEGER NOT NULL,
    descriptionChanged INTEGER NOT NULL,
    stateChanged INTEGER NOT NULL,
    priorityChanged INTEGER NOT NULL,
    dueDateChanged INTEGER NOT NULL,
    dueTimeChanged INTEGER NOT NULL,

    completedAt INTEGER,

    isDeleted TEXT NOT NULL,
    deletedChanged INTEGER NOT NULL,
    userId TEXT NOT NULL,

    FOREIGN KEY (userId) REFERENCES users(uuid) ON DELETE CASCADE,

    PRIMARY KEY (uuid, userId)
);

CREATE TABLE IF NOT EXISTS constants (
    key TEXT NOT NULL UNIQUE PRIMARY KEY,
    value TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
    uuid TEXT NOT NULL UNIQUE PRIMARY KEY,
    username TEXT NOT NULL UNIQUE,
    passwordHash TEXT NOT NULL
);