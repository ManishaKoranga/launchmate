package com.launchmate.website.generation;

import com.launchmate.shared.persistence.TimestampedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "generation_jobs")
public class GenerationJob extends TimestampedEntity {

    public enum Status { QUEUED, RUNNING, SUCCEEDED, FAILED }

    @Id
    private UUID id;

    @Column(name = "business_id", nullable = false, updatable = false)
    private UUID businessId;

    @Column(name = "requested_by")
    private UUID requestedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GenerationMode mode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private List<JobStep> steps;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected GenerationJob() {
    }

    GenerationJob(UUID businessId, UUID requestedBy, GenerationMode mode, List<JobStep> steps) {
        this.id = UUID.randomUUID();
        this.businessId = businessId;
        this.requestedBy = requestedBy;
        this.mode = mode;
        this.status = Status.QUEUED;
        this.steps = new ArrayList<>(steps);
    }

    void startStep(String key) {
        status = Status.RUNNING;
        setStep(key, JobStep.StepStatus.RUNNING);
    }

    void completeStep(String key) {
        setStep(key, JobStep.StepStatus.DONE);
    }

    void fail(String message, Instant now) {
        status = Status.FAILED;
        errorMessage = message.length() > 500 ? message.substring(0, 500) : message;
        steps = steps.stream().map(s -> s.status() == JobStep.StepStatus.RUNNING ? s.with(JobStep.StepStatus.FAILED) : s)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        completedAt = now;
    }

    void succeed(Instant now) {
        status = Status.SUCCEEDED;
        completedAt = now;
    }

    boolean isActive() {
        return status == Status.QUEUED || status == Status.RUNNING;
    }

    private void setStep(String key, JobStep.StepStatus s) {
        List<JobStep> copy = new ArrayList<>(steps);
        copy.replaceAll(step -> step.key().equals(key) ? step.with(s) : step);
        steps = copy; // new list instance so Hibernate detects the JSON change
    }

    public UUID getId() { return id; }
    public UUID getBusinessId() { return businessId; }
    public UUID getRequestedBy() { return requestedBy; }
    public GenerationMode getMode() { return mode; }
    public Status getStatus() { return status; }
    public List<JobStep> getSteps() { return List.copyOf(steps); }
    public String getErrorMessage() { return errorMessage; }
    public Instant getCompletedAt() { return completedAt; }
}
