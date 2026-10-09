ALTER TABLE public.manager_operations ENABLE ROW LEVEL SECURITY;
REVOKE ALL ON TABLE public.manager_operations FROM PUBLIC;
REVOKE ALL ON SEQUENCE public.manager_operations_id_seq FROM PUBLIC;
DO $$
DECLARE client_role text;
BEGIN
    IF current_user IN ('anon', 'authenticated') THEN
        RAISE EXCEPTION 'Application migrations must not use a client database role';
    END IF;
    EXECUTE format('CREATE POLICY manager_operations_backend_access ON public.manager_operations FOR ALL TO %I USING (true) WITH CHECK (true)', current_user);
    FOREACH client_role IN ARRAY ARRAY['anon', 'authenticated'] LOOP
        IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = client_role) THEN
            EXECUTE format('REVOKE ALL ON TABLE public.manager_operations FROM %I', client_role);
            EXECUTE format('REVOKE ALL ON SEQUENCE public.manager_operations_id_seq FROM %I', client_role);
        END IF;
    END LOOP;
END
$$;
