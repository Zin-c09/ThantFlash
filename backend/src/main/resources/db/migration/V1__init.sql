CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    display_name  VARCHAR(100) NOT NULL,
    time_zone     VARCHAR(64)  NOT NULL DEFAULT 'Asia/Yangon',
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE decks (
    id         BIGSERIAL PRIMARY KEY,
    owner_id   BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name       VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_deck_owner_name UNIQUE (owner_id, name)
);

CREATE TABLE cards (
    id            BIGSERIAL PRIMARY KEY,
    deck_id       BIGINT        NOT NULL REFERENCES decks (id) ON DELETE CASCADE,
    front         VARCHAR(2000) NOT NULL,
    back          VARCHAR(2000) NOT NULL,
    interval_days DOUBLE PRECISION NOT NULL DEFAULT 0,
    ease          DOUBLE PRECISION NOT NULL DEFAULT 2.5,
    reps          INTEGER       NOT NULL DEFAULT 0,
    due_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    version       BIGINT        NOT NULL DEFAULT 0
);
CREATE INDEX idx_cards_deck_due ON cards (deck_id, due_at);

CREATE TABLE review_logs (
    id            BIGSERIAL PRIMARY KEY,
    card_id       BIGINT   NOT NULL REFERENCES cards (id) ON DELETE CASCADE,
    user_id       BIGINT   NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    grade         SMALLINT NOT NULL,
    interval_days DOUBLE PRECISION NOT NULL,
    reviewed_at   TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_review_logs_user_time ON review_logs (user_id, reviewed_at);

CREATE TABLE reminders (
    id         BIGSERIAL PRIMARY KEY,
    owner_id   BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    title      VARCHAR(200) NOT NULL,
    fire_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    repeat     VARCHAR(10)  NOT NULL,
    done       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_reminders_due ON reminders (done, fire_at);
