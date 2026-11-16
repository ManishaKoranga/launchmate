package com.launchmate.crm.controller;

import com.launchmate.crm.domain.LeadSource;
import com.launchmate.crm.domain.LeadStatus;
import com.launchmate.crm.dto.InquiryRequest;
import com.launchmate.crm.dto.LeadResponse;
import com.launchmate.crm.dto.LeadSummary;
import com.launchmate.crm.service.LeadService;
import com.launchmate.shared.audit.ActorType;
import com.launchmate.shared.tenant.TenantContext;
import com.launchmate.shared.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Validated
@RequestMapping("/api/businesses/{businessId}/leads")
@Tag(name = "CRM")
public class LeadController {

    private final LeadService leadService;
    private final TenantContext tenant;

    public LeadController(LeadService leadService, TenantContext tenant) {
        this.leadService = leadService;
        this.tenant = tenant;
    }

    public record StatusRequest(@NotNull LeadStatus status) {
    }

    public record NotesRequest(@Size(max = 4000) String notes) {
    }

    public record QuoteRequest(@Size(max = 4000) String quoteDraft) {
    }

    @Operation(summary = "List inquiries, newest first")
    @GetMapping
    public PageResponse<LeadResponse> list(@PathVariable UUID businessId,
                                           @RequestParam(required = false) LeadStatus status,
                                           @RequestParam(defaultValue = "0") @Min(0) int page,
                                           @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return PageResponse.of(leadService.list(tenant.businessId(), status, page, size), LeadResponse::from);
    }

    @Operation(summary = "Counts per status")
    @GetMapping("/summary")
    public LeadSummary summary(@PathVariable UUID businessId) {
        return leadService.summary(tenant.businessId());
    }

    @Operation(summary = "Get an inquiry")
    @GetMapping("/{leadId}")
    public LeadResponse get(@PathVariable UUID businessId, @PathVariable UUID leadId) {
        return LeadResponse.from(leadService.get(tenant.businessId(), leadId));
    }

    @Operation(summary = "Record an inquiry received by phone or in person")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LeadResponse create(@PathVariable UUID businessId, @Valid @RequestBody InquiryRequest body) {
        return LeadResponse.from(leadService.create(tenant.businessId(), LeadSource.MANUAL, body, tenant.userId()));
    }

    @Operation(summary = "Move an inquiry to another status")
    @PatchMapping("/{leadId}/status")
    public LeadResponse status(@PathVariable UUID businessId, @PathVariable UUID leadId, @Valid @RequestBody StatusRequest body) {
        return LeadResponse.from(leadService.changeStatus(tenant.businessId(), tenant.userId(), ActorType.USER, leadId,
                body.status()));
    }

    @Operation(summary = "Update private notes")
    @PutMapping("/{leadId}/notes")
    public LeadResponse notes(@PathVariable UUID businessId, @PathVariable UUID leadId, @Valid @RequestBody NotesRequest body) {
        return LeadResponse.from(leadService.updateNotes(tenant.businessId(), tenant.userId(), leadId, body.notes()));
    }

    @Operation(summary = "Save or edit the draft quotation")
    @PutMapping("/{leadId}/quote")
    public LeadResponse quote(@PathVariable UUID businessId, @PathVariable UUID leadId, @Valid @RequestBody QuoteRequest body) {
        return LeadResponse.from(leadService.saveQuoteDraft(tenant.businessId(), tenant.userId(), ActorType.USER, leadId,
                body.quoteDraft()));
    }
}
