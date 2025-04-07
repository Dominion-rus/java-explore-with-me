-- Удаляем таблицы, если они существуют
DROP TABLE IF EXISTS hits;

CREATE TABLE IF NOT EXISTS hits (
    id SERIAL PRIMARY KEY,
    app VARCHAR(255) NOT NULL,
    uri VARCHAR(512) NOT NULL,
    ip VARCHAR(64) NOT NULL,
    timestamp TIMESTAMP NOT NULL
);
