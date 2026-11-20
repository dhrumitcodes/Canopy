ALTER TABLE events ADD COLUMN reminder_sent_at TIMESTAMP;
CREATE INDEX idx_events_pending_review ON events(status, society_id);
CREATE INDEX idx_events_reminder_due ON events(status, start_time) WHERE reminder_sent_at IS NULL;
