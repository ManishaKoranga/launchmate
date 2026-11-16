package com.launchmate.shared.ratelimit;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory sliding-window counter keyed by an arbitrary string. Adequate for a single instance; a
 * multi-instance deployment would back this with Redis.
 */
public final class SlidingWindowLimiter {

    private static final int MAX_KEYS = 100_000;

    private final Duration window;
    private final Clock clock;
    private final Map<String, Deque<Instant>> hits = new ConcurrentHashMap<>();

    public SlidingWindowLimiter(Duration window, Clock clock) {
        this.window = window;
        this.clock = clock;
    }

    /** Records a hit and returns true if it is within {@code limit}; returns false (without recording) otherwise. */
    public boolean tryAcquire(String key, int limit) {
        if (hits.size() > MAX_KEYS) {
            evictExpired();
        }
        Instant now = clock.instant();
        Deque<Instant> q = hits.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (q) {
            while (!q.isEmpty() && q.peekFirst().isBefore(now.minus(window))) {
                q.pollFirst();
            }
            if (q.size() >= limit) {
                return false;
            }
            q.addLast(now);
            return true;
        }
    }

    private void evictExpired() {
        Instant cutoff = clock.instant().minus(window);
        hits.entrySet().removeIf(e -> {
            synchronized (e.getValue()) {
                return e.getValue().isEmpty() || e.getValue().peekLast().isBefore(cutoff);
            }
        });
    }
}
