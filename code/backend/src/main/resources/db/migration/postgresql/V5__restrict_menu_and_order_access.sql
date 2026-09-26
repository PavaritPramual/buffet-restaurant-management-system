ALTER TABLE menu_categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE menu_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE package_menu_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE order_items ENABLE ROW LEVEL SECURITY;

-- Supabase exposes these roles; plain PostgreSQL does not define them.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'anon') THEN
        REVOKE ALL PRIVILEGES ON TABLE
            menu_categories, menu_items, package_menu_items, orders, order_items
        FROM anon;
        REVOKE ALL PRIVILEGES ON SEQUENCE
            menu_categories_id_seq, menu_items_id_seq, orders_id_seq, order_items_id_seq
        FROM anon;
    END IF;

    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'authenticated') THEN
        REVOKE ALL PRIVILEGES ON TABLE
            menu_categories, menu_items, package_menu_items, orders, order_items
        FROM authenticated;
        REVOKE ALL PRIVILEGES ON SEQUENCE
            menu_categories_id_seq, menu_items_id_seq, orders_id_seq, order_items_id_seq
        FROM authenticated;
    END IF;
END
$$;
