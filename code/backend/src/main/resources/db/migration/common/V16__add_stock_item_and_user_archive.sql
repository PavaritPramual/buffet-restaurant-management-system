-- Forward-only R01-C migration (V16). Do not edit V1-V15.
-- Archive state is separate from the existing temporary `active` flag. Existing rows get
-- archived_at NULL, so quantities, targets, roles and history are untouched.
ALTER TABLE stock_items ADD COLUMN archived_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE app_users ADD COLUMN archived_at TIMESTAMP WITH TIME ZONE;

-- Stock history must keep its actor. Accounts with history are archived instead of deleted, so the
-- database now refuses to delete a referenced account rather than silently nulling the actor.
-- Legacy rows with a NULL actor stay valid.
ALTER TABLE stock_transactions DROP CONSTRAINT fk_stock_transaction_actor;
ALTER TABLE stock_transactions ADD CONSTRAINT fk_stock_transaction_actor
    FOREIGN KEY (actor_user_id) REFERENCES app_users(id) ON DELETE RESTRICT;

CREATE INDEX idx_stock_transactions_actor ON stock_transactions(actor_user_id);
