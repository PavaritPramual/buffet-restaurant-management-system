-- V19: recipe at order time, consumption at kitchen start. Existing rows stay untracked.
ALTER TABLE menu_items ADD COLUMN automatic_stock_deduction BOOLEAN NOT NULL DEFAULT FALSE;
CREATE TABLE menu_stock_usage (
    menu_item_id BIGINT NOT NULL,
    stock_item_id BIGINT NOT NULL,
    quantity_per_serving DECIMAL(12,3) NOT NULL CHECK (quantity_per_serving > 0),
    stock_unit VARCHAR(24) NOT NULL,
    PRIMARY KEY (menu_item_id, stock_item_id),
    CONSTRAINT fk_menu_usage_menu FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE,
    CONSTRAINT fk_menu_usage_stock FOREIGN KEY (stock_item_id) REFERENCES stock_items(id) ON DELETE RESTRICT
);
CREATE INDEX idx_menu_usage_stock ON menu_stock_usage(stock_item_id);
CREATE TABLE order_item_stock_usage (
    order_item_id BIGINT NOT NULL,
    stock_item_id BIGINT NOT NULL,
    quantity_per_serving DECIMAL(12,3) NOT NULL CHECK (quantity_per_serving > 0),
    stock_unit VARCHAR(24) NOT NULL,
    PRIMARY KEY (order_item_id, stock_item_id),
    CONSTRAINT fk_order_usage_item FOREIGN KEY (order_item_id) REFERENCES order_items(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_usage_stock FOREIGN KEY (stock_item_id) REFERENCES stock_items(id) ON DELETE RESTRICT
);
CREATE INDEX idx_order_usage_stock ON order_item_stock_usage(stock_item_id);
ALTER TABLE stock_transactions DROP CONSTRAINT chk_stock_transaction_type;
ALTER TABLE stock_transactions ADD CONSTRAINT chk_stock_transaction_type CHECK (transaction_type IN ('IN','ADJUSTMENT','CONSUMPTION'));
ALTER TABLE stock_transactions ADD COLUMN order_id BIGINT;
ALTER TABLE stock_transactions ADD CONSTRAINT fk_consumption_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE RESTRICT;
ALTER TABLE stock_transactions ADD CONSTRAINT chk_consumption_order CHECK ((transaction_type = 'CONSUMPTION' AND order_id IS NOT NULL AND quantity_delta < 0) OR (transaction_type <> 'CONSUMPTION' AND order_id IS NULL));
ALTER TABLE stock_transactions ADD CONSTRAINT uq_consumption_order_stock UNIQUE (order_id, stock_item_id);
-- Backend-only tables. postgres has BYPASSRLS; API authorization remains required.
ALTER TABLE public.menu_stock_usage ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.order_item_stock_usage ENABLE ROW LEVEL SECURITY;
CREATE POLICY menu_stock_usage_postgres ON public.menu_stock_usage FOR ALL TO postgres USING (true) WITH CHECK (true);
CREATE POLICY order_item_stock_usage_postgres ON public.order_item_stock_usage FOR ALL TO postgres USING (true) WITH CHECK (true);
DO $$
DECLARE app_table text; client_role text; backend_role name := current_user;
BEGIN
    IF backend_role IN ('anon', 'authenticated') THEN RAISE EXCEPTION 'Migration requires backend role'; END IF;
    FOREACH app_table IN ARRAY ARRAY['menu_stock_usage', 'order_item_stock_usage'] LOOP
        EXECUTE format('REVOKE ALL ON TABLE public.%I FROM PUBLIC', app_table);
        FOREACH client_role IN ARRAY ARRAY['anon', 'authenticated'] LOOP
            IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = client_role) THEN
                EXECUTE format('REVOKE ALL ON TABLE public.%I FROM %I', app_table, client_role);
            END IF;
        END LOOP;
        IF backend_role <> 'postgres' THEN
            EXECUTE format('CREATE POLICY %I ON public.%I FOR ALL TO %I USING (true) WITH CHECK (true)', app_table || '_backend', app_table, backend_role);
        END IF;
        EXECUTE format('GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE public.%I TO %I', app_table, backend_role);
        EXECUTE format('GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE public.%I TO postgres', app_table);
    END LOOP;
END $$;
