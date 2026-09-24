CREATE TABLE buffet_packages (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10, 2) NOT NULL CHECK (price > 0),
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE soups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

-- React uses the Spring Boot API; these tables are not client-facing Supabase APIs.
ALTER TABLE buffet_packages ENABLE ROW LEVEL SECURITY;
ALTER TABLE soups ENABLE ROW LEVEL SECURITY;
REVOKE ALL ON TABLE buffet_packages, soups FROM anon, authenticated;
REVOKE ALL ON SEQUENCE buffet_packages_id_seq, soups_id_seq FROM anon, authenticated;