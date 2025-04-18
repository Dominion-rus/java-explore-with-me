-- Удаляем таблицы, если они существуют
DROP TABLE IF EXISTS users, categories, events, requests, compilations, compilation_events, comments;

-- Пользователи
CREATE TABLE IF NOT EXISTS users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(250) NOT NULL,
    email VARCHAR(254) NOT NULL UNIQUE
);

-- Категории событий
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- События
CREATE TABLE IF NOT EXISTS events (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    annotation VARCHAR(2000) NOT NULL,
    category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    created_on TIMESTAMP NOT NULL,
    description VARCHAR(7000) NOT NULL,
    event_date TIMESTAMP NOT NULL,
    initiator_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    location_lat FLOAT NOT NULL,
    location_lon FLOAT NOT NULL,
    paid BOOLEAN NOT NULL,
    participant_limit INTEGER NOT NULL,
    published_on TIMESTAMP,
    request_moderation BOOLEAN NOT NULL,
    state VARCHAR(32) NOT NULL,
    title VARCHAR(120) NOT NULL,
    views BIGINT DEFAULT 0
);

-- Запросы на участие в событиях
CREATE TABLE IF NOT EXISTS requests (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    created TIMESTAMP NOT NULL,
    event_id BIGINT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    requester_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL,
    UNIQUE (event_id, requester_id)
);

-- Подборки событий
CREATE TABLE IF NOT EXISTS compilations (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    pinned BOOLEAN NOT NULL DEFAULT FALSE
);

-- Связь подборки и событий (многие ко многим)
CREATE TABLE IF NOT EXISTS compilation_events (
    compilation_id BIGINT NOT NULL REFERENCES compilations(id) ON DELETE CASCADE,
    event_id BIGINT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    PRIMARY KEY (compilation_id, event_id)
);

-- Комментарии
CREATE TABLE IF NOT EXISTS comments (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    author_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    text TEXT NOT NULL,
    created_on TIMESTAMP NOT NULL,
    updated_on TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    moderator_comment TEXT,

    CONSTRAINT fk_comment_author FOREIGN KEY (author_id)
        REFERENCES users(id) ON DELETE CASCADE,

    CONSTRAINT fk_comment_event FOREIGN KEY (event_id)
        REFERENCES events(id) ON DELETE CASCADE
);


