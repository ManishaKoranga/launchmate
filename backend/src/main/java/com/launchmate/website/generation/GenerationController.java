package com.launchmate.website.generation;

import com.launchmate.shared.tenant.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/businesses/{businessId}/website/generation-jobs")
@Tag(name = "Website")
public class GenerationController {

    private final GenerationJobService jobService;
    private final TenantContext tenant;

    public GenerationController(GenerationJobService jobService, TenantContext tenant) {
        this.jobService = jobService;
        this.tenant = tenant;
    }

    public record StartRequest(@NotNull GenerationMode mode) {
    }

    @Operation(summary = "Start generating the website draft (AI or template starter content)")
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public GenerationJobResponse start(@PathVariable UUID businessId, @Valid @RequestBody StartRequest body) {
        return toResponse(jobService.start(tenant.businessId(), tenant.userId(), body.mode()));
    }

    @Operation(summary = "Poll a generation job's real progress")
    @GetMapping("/{jobId}")
    public GenerationJobResponse get(@PathVariable UUID businessId, @PathVariable UUID jobId) {
        return toResponse(jobService.get(tenant.businessId(), jobId));
    }

    private GenerationJobResponse toResponse(GenerationJob j) {
        boolean stale = jobService.isStale(j);
        return new GenerationJobResponse(j.getId(), j.getMode(),
                stale ? GenerationJob.Status.FAILED : j.getStatus(), j.getSteps(),
                stale ? "This generation was interrupted. Please try again." : j.getErrorMessage(),
                j.getCreatedAt(), j.getCompletedAt());
    }
}
