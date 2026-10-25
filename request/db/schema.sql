
CREATE TABLE clients (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    email VARCHAR(255) NOT NULL
);

CREATE TABLE reservations (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    client_id BIGINT NOT NULL REFERENCES clients(id),
    start_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    cancel_reason TEXT,
    CONSTRAINT valid_reservation_status
        CHECK (status IN ('ACTIVE', 'CANCELLED'))
);

CREATE INDEX idx_reservations_start_time
    ON reservations(start_time);

CREATE INDEX idx_reservations_client_id
    ON reservations(client_id);

CREATE TABLE working_hours (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    day_of_week SMALLINT NOT NULL UNIQUE
        CHECK (day_of_week BETWEEN 1 AND 7),
    open_time TIME NOT NULL,
    close_time TIME NOT NULL,
    CHECK (open_time < close_time)
);

INSERT INTO working_hours (day_of_week, open_time, close_time)
VALUES
    (1, '08:00', '18:00'),
    (2, '08:00', '18:00'),
    (3, '08:00', '18:00'),
    (4, '08:00', '18:00'),
    (5, '08:00', '18:00'),
    (6, '08:00', '18:00'),
    (7, '08:00', '18:00')
ON CONFLICT (day_of_week)
DO UPDATE SET
    open_time = EXCLUDED.open_time,
    close_time = EXCLUDED.close_time;
