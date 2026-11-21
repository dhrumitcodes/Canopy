-- Keep the V13 migration immutable for databases where it has already run.
-- Hibernate maps EventFeedback.rating as Integer, so align PostgreSQL SMALLINT with INTEGER.
ALTER TABLE event_feedback
    ALTER COLUMN rating TYPE INTEGER
    USING rating::INTEGER;
