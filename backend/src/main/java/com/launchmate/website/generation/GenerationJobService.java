package com.launchmate.website.generation;

import com.launchmate.business.domain.PlanLimits;
import com.launchmate.business.service.BusinessService;
import com.launchmate.shared.exception.ResourceNotFoundException;
import com.launchmate.shared.ratelimit.AiUsageGuard;
import com.launchmate.website.generation.JobStep.StepStatus;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class GenerationJobService {

    /** A job that has not progressed for this long was interrupted (e.g. server restart). */
    static final Duration STALE_AFTER = Duration.ofMinutes(5);

    static final String STEP_PROFILE = "profile";
    static final String STEP_CONTENT = "content";
    static final String STEP_VALIDATE = "validate";
    static final String STEP_SAVE = "save";

    private final GenerationJobRepository jobs;
    private final ApplicationEventPublisher events;
    private final Clock clock;
    private final AiUsageGuard aiUsage;
    private final BusinessService businesses;

    public GenerationJobService(GenerationJobRepository jobs, ApplicationEventPublisher events, Clock clock,
                                AiUsageGuard aiUsage, BusinessService businesses) {
        this.jobs = jobs;
        this.events = events;
        this.clock = clock;
        this.aiUsage = aiUsage;
        this.businesses = businesses;
    }

    /** Starts a generation job, or returns the one already running for this business. */
    @Transactional
    public GenerationJob start(UUID businessId, UUID userId, GenerationMode mode) {
        Optional<GenerationJob> running = jobs.findTop5ByBusinessIdOrderByCreatedAtDesc(businessId).stream()
                .filter(j -> j.isActive() && !isStale(j)).findFirst();
        if (running.isPresent()) {
            return running.get();
        }
        if (mode == GenerationMode.AI) {
            aiUsage.acquire(userId, PlanLimits.of(businesses.get(businessId).getPlan()).aiRequestsPerHour());
        }
        GenerationJob job = jobs.save(new GenerationJob(businessId, userId, mode, steps(mode)));
        events.publishEvent(new GenerationRequested(job.getId(), businessId, userId));
        return job;
    }

    @Transactional(readOnly = true)
    public GenerationJob get(UUID businessId, UUID jobId) {
        return jobs.findByIdAndBusinessId(jobId, businessId).orElseThrow(() -> new ResourceNotFoundException("Job"));
    }

    boolean isStale(GenerationJob job) {
        return job.isActive() && job.getUpdatedAt() != null
                && job.getUpdatedAt().isBefore(clock.instant().minus(STALE_AFTER));
    }

    private static List<JobStep> steps(GenerationMode mode) {
        String content = mode == GenerationMode.AI ? "Writing your website content with AI" : "Preparing template content";
        return List.of(
                new JobStep(STEP_PROFILE, "Reading your business profile", StepStatus.PENDING),
                new JobStep(STEP_CONTENT, content, StepStatus.PENDING),
                new JobStep(STEP_VALIDATE, "Checking design and content", StepStatus.PENDING),
                new JobStep(STEP_SAVE, "Saving your draft website", StepStatus.PENDING));
    }
}
