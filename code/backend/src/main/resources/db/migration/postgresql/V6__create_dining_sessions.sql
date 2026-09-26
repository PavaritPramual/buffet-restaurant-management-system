CREATE TABLE public.dining_sessions (
    id BIGSERIAL PRIMARY KEY,
    table_id BIGINT NOT NULL,
    package_id BIGINT NOT NULL,
    soup_id BIGINT NOT NULL,
    adult_count INT NOT NULL CHECK (adult_count >= 0),
    child_count INT NOT NULL DEFAULT 0 CHECK (child_count >= 0),
    session_token VARCHAR(100) NOT NULL UNIQUE,
    start_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT fk_dining_sessions_table FOREIGN KEY (table_id)
        REFERENCES public.restaurant_tables(id) ON DELETE RESTRICT,
    CONSTRAINT fk_dining_sessions_package FOREIGN KEY (package_id)
        REFERENCES public.buffet_packages(id) ON DELETE RESTRICT,
    CONSTRAINT fk_dining_sessions_soup FOREIGN KEY (soup_id)
        REFERENCES public.soups(id) ON DELETE RESTRICT
);

CREATE INDEX idx_dining_sessions_status ON public.dining_sessions(status);

ALTER TABLE public.dining_sessions ENABLE ROW LEVEL SECURITY;

CREATE POLICY dining_sessions_backend_access ON public.dining_sessions
    FOR ALL TO CURRENT_USER USING (true) WITH CHECK (true);

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'anon') THEN
        REVOKE ALL ON TABLE public.dining_sessions FROM anon;
        REVOKE ALL ON SEQUENCE public.dining_sessions_id_seq FROM anon;
    END IF;

    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'authenticated') THEN
        REVOKE ALL ON TABLE public.dining_sessions FROM authenticated;
        REVOKE ALL ON SEQUENCE public.dining_sessions_id_seq FROM authenticated;
    END IF;
END
$$;
