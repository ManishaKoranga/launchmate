package com.launchmate.onboarding.controller;

import com.launchmate.business.dto.BusinessResponse;
import com.launchmate.onboarding.dto.CompleteOnboardingRequest;
import com.launchmate.onboarding.dto.OnboardingResponse;
import com.launchmate.onboarding.dto.SendMessageRequest;
import com.launchmate.onboarding.service.OnboardingService;
import com.launchmate.shared.exception.ResourceNotFoundException;
import com.launchmate.shared.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Onboarding happens before a business exists, so it is scoped to the authenticated user. */
@RestController
@RequestMapping("/api/onboarding/conversations")
@Tag(name = "Onboarding")
public class OnboardingController {

    private final OnboardingService onboarding;

    public OnboardingController(OnboardingService onboarding) {
        this.onboarding = onboarding;
    }

    @Operation(summary = "Start a new onboarding conversation")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OnboardingResponse start(@AuthenticationPrincipal AuthenticatedUser user) {
        return onboarding.start(user.id());
    }

    @Operation(summary = "Resume the latest unfinished onboarding (404 if none)")
    @GetMapping("/latest")
    public OnboardingResponse latest(@AuthenticationPrincipal AuthenticatedUser user) {
        return onboarding.latestOpen(user.id()).orElseThrow(() -> new ResourceNotFoundException("Onboarding"));
    }

    @Operation(summary = "Get an onboarding conversation")
    @GetMapping("/{conversationId}")
    public OnboardingResponse get(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID conversationId) {
        return onboarding.get(user.id(), conversationId);
    }

    @Operation(summary = "Send a message; the AI updates the draft profile and replies")
    @PostMapping("/{conversationId}/messages")
    public OnboardingResponse send(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID conversationId,
                                   @Valid @RequestBody SendMessageRequest body) {
        return onboarding.sendMessage(user.id(), conversationId, body.text());
    }

    @Operation(summary = "Approve the profile and create the business")
    @PostMapping("/{conversationId}/complete")
    @ResponseStatus(HttpStatus.CREATED)
    public BusinessResponse complete(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID conversationId,
                                     @Valid @RequestBody CompleteOnboardingRequest body) {
        return BusinessResponse.from(onboarding.complete(user.id(), conversationId, body));
    }
}
