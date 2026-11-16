package com.launchmate.business.controller;

import com.launchmate.business.domain.BusinessProfile;
import com.launchmate.business.dto.BusinessResponse;
import com.launchmate.business.dto.BusinessSummary;
import com.launchmate.business.dto.UpdateHoursRequest;
import com.launchmate.business.service.BusinessService;
import com.launchmate.shared.security.AuthenticatedUser;
import com.launchmate.shared.tenant.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/businesses")
@Tag(name = "Businesses")
public class BusinessController {

    private final BusinessService businessService;
    private final TenantContext tenant;

    public BusinessController(BusinessService businessService, TenantContext tenant) {
        this.businessService = businessService;
        this.tenant = tenant;
    }

    @Operation(summary = "Businesses the current user belongs to")
    @GetMapping
    public List<BusinessSummary> list(@AuthenticationPrincipal AuthenticatedUser user) {
        return businessService.listForUser(user.id()).stream().map(BusinessSummary::from).toList();
    }

    @Operation(summary = "Create a business; the caller becomes its owner")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BusinessResponse create(@AuthenticationPrincipal AuthenticatedUser user,
                                   @Valid @RequestBody BusinessProfile profile) {
        return BusinessResponse.from(businessService.create(user.id(), profile));
    }

    @Operation(summary = "Business profile")
    @GetMapping("/{businessId}")
    public BusinessResponse get(@PathVariable UUID businessId) {
        return BusinessResponse.from(businessService.get(tenant.businessId()));
    }

    @Operation(summary = "Replace the business profile")
    @PutMapping("/{businessId}")
    public BusinessResponse update(@PathVariable UUID businessId, @Valid @RequestBody BusinessProfile profile) {
        return BusinessResponse.from(businessService.updateProfile(tenant.businessId(), tenant.userId(), profile));
    }

    @Operation(summary = "Replace opening hours")
    @PutMapping("/{businessId}/hours")
    public BusinessResponse updateHours(@PathVariable UUID businessId, @Valid @RequestBody UpdateHoursRequest body) {
        return BusinessResponse.from(businessService.updateOpeningHours(tenant.businessId(), tenant.userId(),
                body.openingHours()));
    }

    @Operation(summary = "Owner approves the profile (onboarding step)")
    @PostMapping("/{businessId}/confirm-profile")
    public BusinessResponse confirm(@PathVariable UUID businessId) {
        return BusinessResponse.from(businessService.confirmProfile(tenant.businessId(), tenant.userId()));
    }
}
