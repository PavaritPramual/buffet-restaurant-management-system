ALTER TABLE menu_categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE menu_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE package_menu_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE order_items ENABLE ROW LEVEL SECURITY;

REVOKE ALL PRIVILEGES ON TABLE
    menu_categories,
    menu_items,
    package_menu_items,
    orders,
    order_items
FROM anon, authenticated;

REVOKE ALL PRIVILEGES ON SEQUENCE
    menu_categories_id_seq,
    menu_items_id_seq,
    orders_id_seq,
    order_items_id_seq
FROM anon, authenticated;
