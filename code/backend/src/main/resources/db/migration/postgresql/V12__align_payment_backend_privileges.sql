-- Forward migration: do not edit V9 if it has already been applied.
-- Flyway and Hibernate use the same JDBC datasource in this application.
-- CURRENT_USER is the actual database role, not a Supabase REST/JWT role.
ALTER TABLE public.payments ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS payments_backend_access ON public.payments;

DO $$
DECLARE
    backend_role name := current_user;
    identity_sequence text := pg_get_serial_sequence('public.payments', 'id');
    client_role text;
BEGIN
    IF backend_role IN ('anon', 'authenticated') THEN
        RAISE EXCEPTION 'Payment backend must not use a client database role';
    END IF;
    IF identity_sequence IS NULL THEN
        RAISE EXCEPTION 'Payment identity sequence was not found';
    END IF;

    EXECUTE format('CREATE POLICY payments_backend_access ON public.payments FOR ALL TO %I USING (true) WITH CHECK (true)', backend_role);
    REVOKE ALL ON TABLE public.payments FROM PUBLIC;
    EXECUTE format('REVOKE ALL ON SEQUENCE %s FROM PUBLIC', identity_sequence);
    EXECUTE format('GRANT SELECT, INSERT, UPDATE ON TABLE public.payments TO %I', backend_role);
    EXECUTE format('GRANT USAGE, SELECT ON SEQUENCE %s TO %I', identity_sequence, backend_role);

    FOREACH client_role IN ARRAY ARRAY['anon', 'authenticated'] LOOP
        IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = client_role) THEN
            EXECUTE format('REVOKE ALL ON TABLE public.payments FROM %I', client_role);
            EXECUTE format('REVOKE ALL ON SEQUENCE %s FROM %I', identity_sequence, client_role);
        END IF;
    END LOOP;
END
$$;
