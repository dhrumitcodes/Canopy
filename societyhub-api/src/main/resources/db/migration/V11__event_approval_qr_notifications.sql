-- Event moderation, QR attendance and in-app notifications.
ALTER TABLE events ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'APPROVED';
ALTER TABLE events ADD COLUMN review_note TEXT;
ALTER TABLE events ADD COLUMN reviewed_at TIMESTAMP;
ALTER TABLE events ADD COLUMN reviewed_by BIGINT REFERENCES users(id);
ALTER TABLE events ADD COLUMN created_by BIGINT REFERENCES users(id);
ALTER TABLE event_registrations ADD COLUMN qr_token VARCHAR(64);
ALTER TABLE event_registrations ADD COLUMN checked_in_at TIMESTAMP;
UPDATE event_registrations SET qr_token = md5(random()::text || clock_timestamp()::text || id::text);
ALTER TABLE event_registrations ALTER COLUMN qr_token SET NOT NULL;
ALTER TABLE event_registrations ADD CONSTRAINT uk_event_registrations_qr_token UNIQUE (qr_token);
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(40) NOT NULL,
    title VARCHAR(180) NOT NULL,
    message TEXT NOT NULL,
    related_event_id BIGINT REFERENCES events(id) ON DELETE SET NULL,
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_notifications_user_created ON notifications(user_id, created_at DESC);
CREATE INDEX idx_notifications_user_unread ON notifications(user_id, read_at);
