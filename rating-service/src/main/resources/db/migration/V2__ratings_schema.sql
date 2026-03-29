CREATE TABLE IF NOT EXISTS ratings (
    user_id  UUID,
    username VARCHAR(50) NOT NULL,
    format   VARCHAR(50) NOT NULL,
    elo      INTEGER NOT NULL DEFAULT 1000,
    wins     INTEGER NOT NULL DEFAULT 0,
    losses   INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id, format)
);

CREATE TABLE IF NOT EXISTS battle_history (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    player1_id UUID,
    player2_id UUID,
    winner_id  UUID,
    format     VARCHAR(50) NOT NULL,
    started_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ended_at   TIMESTAMPTZ DEFAULT NOW(),
    end_reason VARCHAR(50)
);

CREATE INDEX IF NOT EXISTS idx_ratings_format_elo ON ratings(format, elo DESC);
