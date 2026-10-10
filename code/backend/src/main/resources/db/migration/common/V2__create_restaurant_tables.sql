CREATE TABLE IF NOT EXISTS restaurant_tables (
    id BIGSERIAL PRIMARY KEY,
    table_number VARCHAR(20) NOT NULL UNIQUE,
    capacity INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE'
);

CREATE INDEX IF NOT EXISTS idx_tables_status ON restaurant_tables(status);
