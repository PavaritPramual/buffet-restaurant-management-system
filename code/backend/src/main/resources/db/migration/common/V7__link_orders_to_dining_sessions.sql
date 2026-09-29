ALTER TABLE orders
    ADD CONSTRAINT fk_orders_dining_session
    FOREIGN KEY (session_id) REFERENCES dining_sessions(id) ON DELETE RESTRICT;
