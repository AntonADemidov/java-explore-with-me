DROP TABLE IF EXISTS locations, categories, users, events, requests CASCADE;

CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    name VARCHAR(250) NOT NULL,
    email VARCHAR(254) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS categories (
    category_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS locations (
    location_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    lat DOUBLE PRECISION,
    lon DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS events (
    event_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    title VARCHAR(120) NOT NULL,
    annotation VARCHAR(2000) NOT NULL,
    description VARCHAR(7000) NOT NULL,
    event_state VARCHAR(10) NOT NULL,
    event_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_on TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    published_on TIMESTAMP WITHOUT TIME ZONE,
    paid BOOLEAN NOT NULL,
    request_moderation BOOLEAN NOT NULL,
    participant_limit BIGINT NOT NULL,
    location_id BIGINT NOT NULL REFERENCES locations (location_id),
    category_id BIGINT NOT NULL REFERENCES categories (category_id),
    user_id BIGINT NOT NULL REFERENCES users (user_id)
);

CREATE TABLE IF NOT EXISTS requests (
   request_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
   event_id BIGINT NOT NULL REFERENCES events (event_id),
   requester_id BIGINT NOT NULL REFERENCES users (user_id),
   request_state VARCHAR(10) NOT NULL,
   created TIMESTAMP WITHOUT TIME ZONE NOT NULL
);