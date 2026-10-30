package com.societyhub.api.auth;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class AttemptLimiter {

    private static final long FORGET_AFTER_MS = Duration.ofHours(24).toMillis();
    private static final int CLEANUP_THRESHOLD = 5_000;

    private final Map<String, Deque<Long>> attempts = new ConcurrentHashMap<>();
    private final Clock clock;

    public AttemptLimiter() {
        this(Clock.systemUTC());
    }

    AttemptLimiter(Clock clock) {
        this.clock = clock;
    }

    public void assertNotBlocked(String key, int max, Duration window) {
        long now = clock.millis();
        Deque<Long> times = attempts.get(key);
        if (times == null) return;

        synchronized (times) {
            while (!times.isEmpty() && now - times.peekFirst() >= window.toMillis()) {
                times.pollFirst();
            }
            if (times.size() >= max) {
                long waitMs = window.toMillis() - (now - times.peekFirst());
                long minutes = Math.max(1, (waitMs + 59_999) / 60_000);
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                        "Too many attempts. Please try again in " + minutes + (minutes == 1 ? " minute." : " minutes."));
            }
        }
    }

    public void recordAttempt(String key) {
        long now = clock.millis();
        if (attempts.size() > CLEANUP_THRESHOLD) {
            attempts.values().removeIf(d -> d.isEmpty() || now - d.peekLast() > FORGET_AFTER_MS);
        }
        Deque<Long> times = attempts.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (times) {
            times.addLast(now);
        }
    }

    public void clear(String key) {
        attempts.remove(key);
    }
}