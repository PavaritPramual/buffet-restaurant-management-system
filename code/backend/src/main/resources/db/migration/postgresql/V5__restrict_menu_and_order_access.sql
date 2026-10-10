ALTER TABLE public.menu_categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.menu_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.package_menu_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.order_items ENABLE ROW LEVEL SECURITY;

-- Flyway and Spring Boot use the same datasource role. Keep backend access explicit
-- without opening these server-owned tables to PUBLIC or Supabase client roles.
CREATE POLICY menu_categories_backend_access ON public.menu_categories
    FOR ALL TO CURRENT_USER USING (true) WITH CHECK (true);
CREATE POLICY menu_items_backend_access ON public.menu_items
    FOR ALL TO CURRENT_USER USING (true) WITH CHECK (true);
CREATE POLICY package_menu_items_backend_access ON public.package_menu_items
    FOR ALL TO CURRENT_USER USING (true) WITH CHECK (true);
CREATE POLICY orders_backend_access ON public.orders
    FOR ALL TO CURRENT_USER USING (true) WITH CHECK (true);
CREATE POLICY order_items_backend_access ON public.order_items
    FOR ALL TO CURRENT_USER USING (true) WITH CHECK (true);

-- Supabase exposes these roles; plain PostgreSQL does not define them.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'anon') THEN
        REVOKE ALL PRIVILEGES ON TABLE
            public.menu_categories, public.menu_items, public.package_menu_items, public.orders, public.order_items
        FROM anon;
        REVOKE ALL PRIVILEGES ON SEQUENCE
            public.menu_categories_id_seq, public.menu_items_id_seq,
            public.orders_id_seq, public.order_items_id_seq
        FROM anon;
    END IF;

    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'authenticated') THEN
        REVOKE ALL PRIVILEGES ON TABLE
            public.menu_categories, public.menu_items, public.package_menu_items, public.orders, public.order_items
        FROM authenticated;
        REVOKE ALL PRIVILEGES ON SEQUENCE
            public.menu_categories_id_seq, public.menu_items_id_seq,
            public.orders_id_seq, public.order_items_id_seq
        FROM authenticated;
    END IF;
END
$$;
