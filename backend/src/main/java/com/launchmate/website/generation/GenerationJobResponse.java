package com.launchmate.website.generation;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GenerationJobResponse(UUID id, GenerationMode mode, GenerationJob.Status status, List<JobStep> steps,
                                    String errorMessage, Instant createdAt, Instant completedAt) {
}
