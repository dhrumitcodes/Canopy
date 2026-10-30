package com.societyhub.api.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttemptLimiterTest {

    private static final Duration WINDOW = Duration.ofMinutes(15);

    /** A clock the test can move forward. */
    private static class MovableClock extends Clock {
        private Instant now = Instant.parse("2026-01-01T10:00:00Z");

        void advance(Duration d) { now = now.plus(d); }

        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    }

    private MovableClock clock;
    private AttemptLimiter limiter;

    @BeforeEach
    void setUp() {
        clock = new MovableClock();
        limiter = new AttemptLimiter(clock);
    }

    private void record(String key, int times) {
        for (int i = 0; i < times; i++) limiter.recordAttempt(key);
    }

    @Test
    void allowsAttemptsBelowTheLimit() {
        record("login:a", 4);

        assertDoesNotThrow(() -> limiter.assertNotBlocked("login:a", 5, WINDOW));
    }

    @Test
    void blocksOnceTheLimitIsReached() {
        record("login:a", 5);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> limiter.assertNotBlocked("login:a", 5, WINDOW));

        assertEquals(429, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("15 minutes"));
    }

    @Test
    void attemptsExpireAfterTheWindow() {
        record("login:a", 5);
        clock.advance(Duration.ofMinutes(16));

        assertDoesNotThrow(() -> limiter.assertNotBlocked("login:a", 5, WINDOW));
    }

    @Test
    void theWaitTimeShrinksAsTimePasses() {
        record("login:a", 5);
        clock.advance(Duration.ofMinutes(10));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> limiter.assertNotBlocked("login:a", 5, WINDOW));

        assertTrue(ex.getReason().contains("5 minutes"));
    }

    @Test
    void aSuccessfulLoginClearsTheCount() {
        record("login:a", 5);
        limiter.clear("login:a");

        assertDoesNotThrow(() -> limiter.assertNotBlocked("login:a", 5, WINDOW));
    }

    @Test
    void differentKeysDoNotAffectEachOther() {
        record("login:a", 5);

        assertDoesNotThrow(() -> limiter.assertNotBlocked("login:b", 5, WINDOW));
    }

    @Test
    void anUnknownKeyIsNeverBlocked() {
        assertDoesNotThrow(() -> limiter.assertNotBlocked("never-seen", 1, WINDOW));
    }
}