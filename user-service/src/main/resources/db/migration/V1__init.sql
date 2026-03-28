CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username         VARCHAR(50)  NOT NULL UNIQUE,
    email            VARCHAR(255) NOT NULL UNIQUE,
    password_hash    VARCHAR(255),
    oauth2_provider  VARCHAR(50),
    oauth2_subject   VARCHAR(255),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE teams (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id        UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name           VARCHAR(100) NOT NULL,
    format         VARCHAR(50)  NOT NULL DEFAULT 'GEN9OU',
    pokemon_data   JSONB        NOT NULL DEFAULT '[]',
    is_validated   BOOLEAN      NOT NULL DEFAULT false,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_teams_user_id ON teams(user_id);
