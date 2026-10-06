-- Forward-only extension; does not change ACTIVE/COMPLETED lifecycle.
ALTER TABLE dining_sessions ADD COLUMN bill_requested_at TIMESTAMP WITH TIME ZONE DEFAULT NULL;
