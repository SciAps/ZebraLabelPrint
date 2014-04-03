CREATE TABLE LibsResults
(
    _id INTEGER PRIMARY KEY,
    time DATE DEFAULT (datetime('now', 'localtime')),
    title TEXT,
    filename TEXT
);