package com.launchmate.website.controller;

import com.launchmate.shared.tenant.TenantContext;
import com.launchmate.website.dto.SwitchTemplateRequest;
import com.launchmate.website.dto.TemplateResponse;
import com.launchmate.website.dto.UpdateDraftRequest;
import com.launchmate.website.dto.WebsiteResponse;
import com.launchmate.website.service.AiEditService;
import com.launchmate.website.service.WebsiteResponseMapper;
import com.launchmate.website.service.WebsiteService;
import com.launchmate.website.template.TemplateRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Website")
public class WebsiteController {

    private final WebsiteService websiteService;
    private final WebsiteResponseMapper mapper;
    private final TemplateRegistry templates;
    private final TenantContext tenant;
    private final AiEditService aiEditService;

    public WebsiteController(WebsiteService websiteService, WebsiteResponseMapper mapper, TemplateRegistry templates,
                             TenantContext tenant, AiEditService aiEditService) {
        this.aiEditService = aiEditService;
        this.websiteService = websiteService;
        this.mapper = mapper;
        this.templates = templates;
        this.tenant = tenant;
    }

    @Operation(summary = "Available website templates")
    @GetMapping("/api/templates")
    public List<TemplateResponse> templates() {
        return templates.all().stream().map(TemplateResponse::from).toList();
    }

    @Operation(summary = "Website draft and publication status (404 until generated)")
    @GetMapping("/api/businesses/{businessId}/website")
    public WebsiteResponse get(@PathVariable UUID businessId) {
        return mapper.toResponse(websiteService.require(tenant.businessId()));
    }

    @Operation(summary = "Save the draft (does not affect the published site)")
    @PutMapping("/api/businesses/{businessId}/website/draft")
    public WebsiteResponse saveDraft(@PathVariable UUID businessId, @Valid @RequestBody UpdateDraftRequest body) {
        return mapper.toResponse(websiteService.updateDraft(tenant.businessId(), tenant.userId(), body.config(),
                body.expectedRevision()));
    }

    @Operation(summary = "Switch the draft to another template, keeping existing content")
    @PostMapping("/api/businesses/{businessId}/website/draft/template")
    public WebsiteResponse switchTemplate(@PathVariable UUID businessId, @Valid @RequestBody SwitchTemplateRequest body) {
        return mapper.toResponse(websiteService.switchTemplate(tenant.businessId(), tenant.userId(), body.templateId()));
    }

    public record AiEditRequest(@jakarta.validation.constraints.NotBlank
                                @jakarta.validation.constraints.Size(max = 1000) String instruction) {
    }

    @Operation(summary = "Ask AI for an edit proposal (nothing is saved until the owner applies it)")
    @PostMapping("/api/businesses/{businessId}/website/draft/ai-edit")
    public AiEditService.Proposal aiEdit(@PathVariable UUID businessId, @Valid @RequestBody AiEditRequest body) {
        return aiEditService.propose(tenant.businessId(), tenant.userId(), body.instruction());
    }
}
