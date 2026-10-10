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
