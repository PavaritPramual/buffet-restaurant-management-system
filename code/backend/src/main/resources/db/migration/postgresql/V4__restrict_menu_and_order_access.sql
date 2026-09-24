ALTER TABLE menu_categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE menu_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE menu_item_packages ENABLE ROW LEVEL SECURITY;
ALTER TABLE customer_orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE order_items ENABLE ROW LEVEL SECURITY;

REVOKE ALL PRIVILEGES ON TABLE
    menu_categories,
    menu_items,
    menu_item_packages,
    customer_orders,
    order_items
FROM anon, authenticated;

REVOKE ALL PRIVILEGES ON SEQUENCE
    menu_categories_id_seq,
    menu_items_id_seq,
    customer_orders_id_seq,
    order_items_id_seq
FROM anon, authenticated;
