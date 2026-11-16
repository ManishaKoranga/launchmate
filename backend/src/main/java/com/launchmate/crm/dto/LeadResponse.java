package com.launchmate.crm.dto;

import com.launchmate.crm.domain.Lead;
import com.launchmate.crm.domain.LeadSource;
import com.launchmate.crm.domain.LeadStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record LeadResponse(UUID id, String customerName, String phone, String email, String message,
                           UUID productId, String productName, LocalDate preferredDate, String budget,
                           LeadSource source, LeadStatus status, String notes, String quoteDraft,
                           Instant statusChangedAt, Instant createdAt) {

    public static LeadResponse from(Lead l) {
        return new LeadResponse(l.getId(), l.getCustomerName(), l.getPhone(), l.getEmail(), l.getMessage(),
                l.getProductId(), l.getProductName(), l.getPreferredDate(), l.getBudget(), l.getSource(),
                l.getStatus(), l.getNotes(), l.getQuoteDraft(), l.getStatusChangedAt(), l.getCreatedAt());
    }
}
