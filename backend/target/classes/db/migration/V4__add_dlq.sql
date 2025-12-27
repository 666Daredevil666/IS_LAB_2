CREATE TABLE dead_letter_queue
(
    id                BIGSERIAL PRIMARY KEY,
    original_event_id BIGINT    NOT NULL,
    aggregate_type    TEXT      NOT NULL,
    aggregate_id      BIGINT    NOT NULL,
    event_type        TEXT      NOT NULL,
    payload           JSONB     NOT NULL,
    attempts          INT       NOT NULL,
    last_error        TEXT,
    failed_at         TIMESTAMP NOT NULL DEFAULT now()
);

