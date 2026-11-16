package com.launchmate.crm.controller;

import com.launchmate.crm.dto.InquiryRequest;
import com.launchmate.crm.service.PublicInquiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = "Public website")
public class PublicInquiryController {

    private final PublicInquiryService inquiries;

    public PublicInquiryController(PublicInquiryService inquiries) {
        this.inquiries = inquiries;
    }

    @Operation(summary = "Submit a customer inquiry to a published website")
    @PostMapping("/api/public/sites/{slug}/inquiries")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Boolean> submit(@PathVariable String slug, @Valid @RequestBody InquiryRequest body,
                                       HttpServletRequest request) {
        inquiries.submit(slug, body, request.getRemoteAddr());
        // Identical response for accepted and honeypot-discarded submissions, so bots learn nothing.
        return Map.of("received", true);
    }
}
