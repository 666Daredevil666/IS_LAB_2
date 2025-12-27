CREATE TABLE import_operation
(
    id            BIGSERIAL PRIMARY KEY,
    user_name     TEXT      NOT NULL,
    status        TEXT      NOT NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP NOT NULL DEFAULT now(),
    added_count   INT,
    error_message TEXT
);

CREATE TABLE outbox_event
(
    id             BIGSERIAL PRIMARY KEY,
    aggregate_type TEXT      NOT NULL,
    aggregate_id   BIGINT    NOT NULL,
    event_type     TEXT      NOT NULL,
    payload        JSONB     NOT NULL,
    status         TEXT      NOT NULL,
    attempts       INT       NOT NULL DEFAULT 0,
    next_run_at    TIMESTAMP,
    created_at     TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX outbox_event_status_idx ON outbox_event (status, next_run_at NULLS FIRST, id);

CREATE TABLE delivery_log
(
    id                BIGSERIAL PRIMARY KEY,
    event_id          BIGINT    NOT NULL,
    idempotency_token TEXT      NOT NULL,
    status            TEXT      NOT NULL,
    attempts          INT       NOT NULL DEFAULT 0,
    last_error        TEXT,
    received_at       TIMESTAMP NOT NULL DEFAULT now(),
    delivered_at      TIMESTAMP
);

CREATE UNIQUE INDEX delivery_log_token_uq ON delivery_log (idempotency_token);

