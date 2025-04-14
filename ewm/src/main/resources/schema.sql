-- Удаляем таблицы, если они существуют
DROP TABLE IF EXISTS test_table;

CREATE TABLE IF NOT EXISTS test_table (
    id SERIAL PRIMARY KEY,
    app VARCHAR(255) NOT NULL
);