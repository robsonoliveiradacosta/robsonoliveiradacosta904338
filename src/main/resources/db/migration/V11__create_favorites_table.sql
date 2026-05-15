-- V11__create_favorites_table.sql
-- Per-user album favorites. Junction-with-payload (created_at) — see
-- specs/001-user-favorites/spec.md and plan.md.

CREATE TABLE favorites (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT      NOT NULL,
    album_id    BIGINT      NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_favorites_user  FOREIGN KEY (user_id)  REFERENCES users(id)  ON DELETE CASCADE,
    CONSTRAINT fk_favorites_album FOREIGN KEY (album_id) REFERENCES albums(id) ON DELETE CASCADE,
    CONSTRAINT uq_favorites_user_album UNIQUE (user_id, album_id)
);

CREATE INDEX idx_favorites_user_created ON favorites(user_id, created_at DESC);
