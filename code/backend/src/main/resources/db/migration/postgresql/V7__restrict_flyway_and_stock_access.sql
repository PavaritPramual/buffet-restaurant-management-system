ALTER TABLE public.app_users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.stock_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.stock_transactions ENABLE ROW LEVEL SECURITY;

CREATE POLICY app_users_backend_access ON public.app_users
    FOR ALL TO CURRENT_USER USING (true) WITH CHECK (true);
CREATE POLICY user_profiles_backend_access ON public.user_profiles
    FOR ALL TO CURRENT_USER USING (true) WITH CHECK (true);
CREATE POLICY stock_items_backend_access ON public.stock_items
    FOR ALL TO CURRENT_USER USING (true) WITH CHECK (true);
CREATE POLICY stock_transactions_backend_access ON public.stock_transactions
    FOR ALL TO CURRENT_USER USING (true) WITH CHECK (true);

REVOKE ALL PRIVILEGES ON TABLE public.flyway_schema_history FROM PUBLIC;
REVOKE ALL PRIVILEGES ON TABLE
    public.app_users, public.user_profiles, public.stock_items, public.stock_transactions
FROM PUBLIC;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'anon') THEN
        REVOKE ALL PRIVILEGES ON TABLE public.flyway_schema_history FROM anon;
        REVOKE ALL PRIVILEGES ON TABLE
            public.app_users, public.user_profiles, public.stock_items, public.stock_transactions
        FROM anon;
        REVOKE ALL PRIVILEGES ON SEQUENCE
            public.app_users_id_seq, public.stock_items_id_seq, public.stock_transactions_id_seq
        FROM anon;
    END IF;

    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'authenticated') THEN
        REVOKE ALL PRIVILEGES ON TABLE public.flyway_schema_history FROM authenticated;
        REVOKE ALL PRIVILEGES ON TABLE
            public.app_users, public.user_profiles, public.stock_items, public.stock_transactions
        FROM authenticated;
        REVOKE ALL PRIVILEGES ON SEQUENCE
            public.app_users_id_seq, public.stock_items_id_seq, public.stock_transactions_id_seq
        FROM authenticated;
    END IF;
END
$$;