CREATE TABLE menu_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE menu_items (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL REFERENCES menu_categories(id),
    name VARCHAR(120) NOT NULL UNIQUE,
    available BOOLEAN NOT NULL DEFAULT TRUE,
    image_url VARCHAR(500)
);

CREATE TABLE menu_item_packages (
    menu_item_id BIGINT NOT NULL REFERENCES menu_items(id) ON DELETE CASCADE,
    package_id BIGINT NOT NULL,
    PRIMARY KEY (menu_item_id, package_id)
);

CREATE TABLE customer_orders (
    id BIGSERIAL PRIMARY KEY,
    dining_session_id BIGINT NOT NULL,
    table_number VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES customer_orders(id) ON DELETE CASCADE,
    menu_item_id BIGINT NOT NULL,
    item_name VARCHAR(120) NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0)
);

CREATE INDEX idx_menu_items_category ON menu_items(category_id);
CREATE INDEX idx_menu_item_packages_package ON menu_item_packages(package_id);
CREATE INDEX idx_customer_orders_session ON customer_orders(dining_session_id);
CREATE INDEX idx_customer_orders_status ON customer_orders(status);
CREATE INDEX idx_order_items_order ON order_items(order_id);
