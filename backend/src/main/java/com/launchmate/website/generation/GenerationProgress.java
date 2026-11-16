package com.launchmate.website.generation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

/** Commits each progress change immediately so the UI sees real, persisted progress. */
@Component
class GenerationProgress {

    private final GenerationJobRepository jobs;
    private final Clock clock;

    GenerationProgress(GenerationJobRepository jobs, Clock clock) {
        this.jobs = jobs;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void start(UUID jobId, String step) {
        jobs.findById(jobId).ifPresent(j -> j.startStep(step));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(UUID jobId, String step) {
        jobs.findById(jobId).ifPresent(j -> j.completeStep(step));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fail(UUID jobId, String message) {
        jobs.findById(jobId).ifPresent(j -> j.fail(message, clock.instant()));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void succeed(UUID jobId) {
        jobs.findById(jobId).ifPresent(j -> j.succeed(clock.instant()));
    }
}
