package com.launchmate.crm.domain;

import com.launchmate.shared.persistence.TimestampedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** A customer inquiry. All customer-supplied fields are untrusted text. */
@Entity
@Table(name = "leads")
public class Lead extends TimestampedEntity {

    @Id
    private UUID id;

    @Column(name = "business_id", nullable = false, updatable = false)
    private UUID businessId;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    private String phone;
    private String email;

    @Column(nullable = false)
    private String message;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "preferred_date")
    private LocalDate preferredDate;

    private String budget;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeadSource source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeadStatus status;

    private String notes;

    @Column(name = "quote_draft")
    private String quoteDraft;

    @Column(name = "status_changed_at", nullable = false)
    private Instant statusChangedAt;

    protected Lead() {
    }

    public Lead(UUID businessId, LeadSource source, String customerName, String phone, String email, String message,
                UUID productId, String productName, LocalDate preferredDate, String budget, Instant now) {
        this.id = UUID.randomUUID();
        this.businessId = businessId;
        this.source = source;
        this.customerName = customerName.trim();
        this.phone = blankToNull(phone);
        this.email = blankToNull(email);
        this.message = message.trim();
        this.productId = productId;
        this.productName = productName;
        this.preferredDate = preferredDate;
        this.budget = blankToNull(budget);
        this.status = LeadStatus.NEW;
        this.statusChangedAt = now;
    }

    public boolean changeStatus(LeadStatus newStatus, Instant now) {
        if (newStatus == status) {
            return false;
        }
        this.status = newStatus;
        this.statusChangedAt = now;
        return true;
    }

    public void updateNotes(String notes) {
        this.notes = blankToNull(notes);
    }

    public void saveQuoteDraft(String quote) {
        this.quoteDraft = blankToNull(quote);
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    public UUID getId() { return id; }
    public UUID getBusinessId() { return businessId; }
    public String getCustomerName() { return customerName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getMessage() { return message; }
    public UUID getProductId() { return productId; }
    public String getProductName() { return productName; }
    public LocalDate getPreferredDate() { return preferredDate; }
    public String getBudget() { return budget; }
    public LeadSource getSource() { return source; }
    public LeadStatus getStatus() { return status; }
    public String getNotes() { return notes; }
    public String getQuoteDraft() { return quoteDraft; }
    public Instant getStatusChangedAt() { return statusChangedAt; }
}
