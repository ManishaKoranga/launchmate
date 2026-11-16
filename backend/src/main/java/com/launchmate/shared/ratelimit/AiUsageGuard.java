package com.launchmate.shared.ratelimit;

import com.launchmate.shared.exception.RateLimitedException;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.util.UUID;

/** Per-user hourly cap on AI calls (limits come from the business's plan). */
@Component
public class AiUsageGuard {

    private final SlidingWindowLimiter limiter;

    public AiUsageGuard(Clock clock) {
        this.limiter = new SlidingWindowLimiter(Duration.ofHours(1), clock);
    }

    public void acquire(UUID userId, int perHourLimit) {
        if (!limiter.tryAcquire(userId.toString(), perHourLimit)) {
            throw new RateLimitedException("You've reached the hourly limit for AI requests. Please try again later.");
        }
    }
}
