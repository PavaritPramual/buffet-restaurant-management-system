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
    CONSTRAINT chk_dining_sessions_guest_count CHECK (adult_count + child_count >= 1),
    CONSTRAINT fk_dining_sessions_table FOREIGN KEY (table_id)
        REFERENCES public.restaurant_tables(id) ON DELETE RESTRICT,
    CONSTRAINT fk_dining_sessions_package FOREIGN KEY (package_id)
        REFERENCES public.buffet_packages(id) ON DELETE RESTRICT,
    CONSTRAINT fk_dining_sessions_soup FOREIGN KEY (soup_id)
        REFERENCES public.soups(id) ON DELETE RESTRICT
);

CREATE INDEX idx_dining_sessions_status ON public.dining_sessions(status);
CREATE INDEX idx_dining_sessions_table ON public.dining_sessions(table_id);
CREATE INDEX idx_dining_sessions_package ON public.dining_sessions(package_id);
CREATE INDEX idx_dining_sessions_soup ON public.dining_sessions(soup_id);

CREATE TABLE public.customer_session_grants (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_customer_grants_session FOREIGN KEY (session_id)
        REFERENCES public.dining_sessions(id) ON DELETE CASCADE
);
CREATE INDEX idx_customer_grants_session ON public.customer_session_grants(session_id);

ALTER TABLE public.dining_sessions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.customer_session_grants ENABLE ROW LEVEL SECURITY;

CREATE POLICY dining_sessions_backend_access ON public.dining_sessions
    FOR ALL TO postgres USING (true) WITH CHECK (true);
CREATE POLICY customer_grants_backend_access ON public.customer_session_grants
    FOR ALL TO postgres USING (true) WITH CHECK (true);

REVOKE ALL ON TABLE public.dining_sessions, public.customer_session_grants FROM PUBLIC;
REVOKE ALL ON SEQUENCE public.dining_sessions_id_seq, public.customer_session_grants_id_seq FROM PUBLIC;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'anon') THEN
        REVOKE ALL ON TABLE public.dining_sessions FROM anon;
        REVOKE ALL ON SEQUENCE public.dining_sessions_id_seq FROM anon;
        REVOKE ALL ON TABLE public.customer_session_grants FROM anon;
        REVOKE ALL ON SEQUENCE public.customer_session_grants_id_seq FROM anon;
    END IF;

    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'authenticated') THEN
        REVOKE ALL ON TABLE public.dining_sessions FROM authenticated;
        REVOKE ALL ON SEQUENCE public.dining_sessions_id_seq FROM authenticated;
        REVOKE ALL ON TABLE public.customer_session_grants FROM authenticated;
        REVOKE ALL ON SEQUENCE public.customer_session_grants_id_seq FROM authenticated;
    END IF;
END
$$;
