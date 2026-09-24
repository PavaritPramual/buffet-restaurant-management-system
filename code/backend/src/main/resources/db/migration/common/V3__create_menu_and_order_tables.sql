CREATE TABLE menu_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE menu_items (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    available BOOLEAN NOT NULL DEFAULT TRUE,
    image_url VARCHAR(500),
    CONSTRAINT fk_menu_items_category FOREIGN KEY (category_id)
        REFERENCES menu_categories(id) ON DELETE RESTRICT
);

CREATE TABLE package_menu_items (
    package_id BIGINT NOT NULL,
    menu_item_id BIGINT NOT NULL,
    PRIMARY KEY (package_id, menu_item_id),
    CONSTRAINT fk_pmi_menu_item FOREIGN KEY (menu_item_id)
        REFERENCES menu_items(id) ON DELETE CASCADE
);

CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL,
    table_number VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'RECEIVED',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    menu_item_id BIGINT NOT NULL REFERENCES menu_items(id) ON DELETE RESTRICT,
    item_name VARCHAR(100) NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    note VARCHAR(255)
);

CREATE INDEX idx_menu_items_category ON menu_items(category_id);
CREATE INDEX idx_package_menu_items_menu ON package_menu_items(menu_item_id);
CREATE INDEX idx_orders_session ON orders(session_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_order_items_order ON order_items(order_id);
