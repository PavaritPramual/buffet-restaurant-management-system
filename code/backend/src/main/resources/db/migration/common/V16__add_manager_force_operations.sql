-- Forward-only: keep order/payment history and the existing menu foreign key intact.
ALTER TABLE menu_items ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE;

CREATE TABLE manager_operations (
    id BIGSERIAL PRIMARY KEY,
    action VARCHAR(30) NOT NULL,
    resource_id BIGINT NOT NULL,
    resource_label VARCHAR(100) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    actor_id BIGINT NOT NULL,
    actor_username VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
