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

-- Flyway and Spring Boot use the same datasource role (postgres in Supabase).
-- CURRENT_USER binds each policy to that role without granting access to PUBLIC.
CREATE POLICY buffet_packages_backend_access ON public.buffet_packages
    FOR ALL TO CURRENT_USER USING (true) WITH CHECK (true);
CREATE POLICY soups_backend_access ON public.soups
    FOR ALL TO CURRENT_USER USING (true) WITH CHECK (true);

-- Supabase exposes these roles; plain PostgreSQL does not define them.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'anon') THEN
        REVOKE ALL ON TABLE buffet_packages, soups FROM anon;
        REVOKE ALL ON SEQUENCE buffet_packages_id_seq, soups_id_seq FROM anon;
    END IF;

    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'authenticated') THEN
        REVOKE ALL ON TABLE buffet_packages, soups FROM authenticated;
        REVOKE ALL ON SEQUENCE buffet_packages_id_seq, soups_id_seq FROM authenticated;
    END IF;
END
$$;
