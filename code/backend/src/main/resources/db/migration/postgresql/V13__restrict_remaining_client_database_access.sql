-- Step 2 security integration: preserve all applied V1-V12 checksums.
-- Restrict only application-owned tables; never alter Supabase-managed schemas.
ALTER TABLE public.restaurant_tables ENABLE ROW LEVEL SECURITY;

DO $$
DECLARE
    backend_role name := current_user;
    app_table text;
    client_role text;
    sequence_name text;
BEGIN
    IF backend_role IN ('anon', 'authenticated') THEN
        RAISE EXCEPTION 'Application migrations must not use a client database role';
    END IF;

    DROP POLICY IF EXISTS restaurant_tables_backend_access ON public.restaurant_tables;
    EXECUTE format('CREATE POLICY restaurant_tables_backend_access ON public.restaurant_tables FOR ALL TO %I USING (true) WITH CHECK (true)', backend_role);

    FOREACH app_table IN ARRAY ARRAY[
        'restaurant_tables', 'buffet_packages', 'soups', 'menu_categories',
        'menu_items', 'package_menu_items', 'dining_sessions', 'customer_session_grants',
        'orders', 'order_items', 'payments', 'app_users', 'user_profiles',
        'stock_items', 'stock_transactions', 'flyway_schema_history'
    ] LOOP
        EXECUTE format('REVOKE ALL ON TABLE public.%I FROM PUBLIC', app_table);
        FOREACH client_role IN ARRAY ARRAY['anon', 'authenticated'] LOOP
            IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = client_role) THEN
                EXECUTE format('REVOKE ALL ON TABLE public.%I FROM %I', app_table, client_role);
            END IF;
        END LOOP;

        -- Link/profile/history tables have no generated ID sequence.
        SELECT pg_get_serial_sequence(format('public.%I', app_table), 'id')
            INTO sequence_name
            FROM information_schema.columns
            WHERE table_schema = 'public' AND information_schema.columns.table_name = app_table
              AND column_name = 'id';
        IF sequence_name IS NOT NULL THEN
            EXECUTE format('REVOKE ALL ON SEQUENCE %s FROM PUBLIC', sequence_name);
            FOREACH client_role IN ARRAY ARRAY['anon', 'authenticated'] LOOP
                IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = client_role) THEN
                    EXECUTE format('REVOKE ALL ON SEQUENCE %s FROM %I', sequence_name, client_role);
                END IF;
            END LOOP;
        END IF;
    END LOOP;

    EXECUTE format('GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE public.restaurant_tables TO %I', backend_role);
    sequence_name := pg_get_serial_sequence('public.restaurant_tables', 'id');
    EXECUTE format('GRANT USAGE, SELECT ON SEQUENCE %s TO %I', sequence_name, backend_role);
END
$$;
