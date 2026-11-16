package com.launchmate.crm.service;

import com.launchmate.catalogue.domain.Product;
import com.launchmate.catalogue.service.ProductService;
import com.launchmate.crm.domain.Lead;
import com.launchmate.crm.domain.LeadSource;
import com.launchmate.crm.domain.LeadStatus;
import com.launchmate.crm.dto.InquiryRequest;
import com.launchmate.crm.dto.LeadSummary;
import com.launchmate.crm.repository.LeadRepository;
import com.launchmate.shared.audit.ActorType;
import com.launchmate.shared.audit.AuditEvent;
import com.launchmate.shared.audit.AuditService;
import com.launchmate.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** CRM use cases. Every lookup is constrained to the authorised business. */
@Service
public class LeadService {

    private final LeadRepository leads;
    private final ProductService products;
    private final AuditService audit;
    private final Clock clock;

    public LeadService(LeadRepository leads, ProductService products, AuditService audit, Clock clock) {
        this.leads = leads;
        this.products = products;
        this.audit = audit;
        this.clock = clock;
    }

    /** Stores an inquiry. The caller has already resolved {@code businessId} from a published site or the owner's session. */
    @Transactional
    public Lead create(UUID businessId, LeadSource source, InquiryRequest req, UUID actorUserId) {
        Optional<Product> product = Optional.ofNullable(req.productId())
                .flatMap(id -> products.listVisible(businessId).stream().filter(p -> p.getId().equals(id)).findFirst());
        Lead lead = leads.save(new Lead(businessId, source, req.name(), req.phone(), req.email(), req.message(),
                product.map(Product::getId).orElse(null), product.map(Product::getName).orElse(null),
                req.preferredDate(), req.budget(), clock.instant()));
        ActorType actor = source == LeadSource.WEBSITE ? ActorType.PUBLIC_VISITOR : ActorType.USER;
        audit.record(new AuditEvent(businessId, actorUserId, actor, "lead.created", "Lead", lead.getId().toString(),
                Map.of("source", source.name())));
        return lead;
    }

    @Transactional(readOnly = true)
    public Page<Lead> list(UUID businessId, LeadStatus status, int page, int size) {
        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return status == null ? leads.findByBusinessId(businessId, pr) : leads.findByBusinessIdAndStatus(businessId, status, pr);
    }

    @Transactional(readOnly = true)
    public Lead get(UUID businessId, UUID leadId) {
        return leads.findByIdAndBusinessId(leadId, businessId).orElseThrow(() -> new ResourceNotFoundException("Lead"));
    }

    @Transactional
    public Lead changeStatus(UUID businessId, UUID actorUserId, ActorType actorType, UUID leadId, LeadStatus status) {
        Lead lead = get(businessId, leadId);
        LeadStatus from = lead.getStatus();
        if (lead.changeStatus(status, clock.instant())) {
            audit.record(new AuditEvent(businessId, actorUserId, actorType, "lead.status_changed", "Lead",
                    leadId.toString(), Map.of("from", from.name(), "to", status.name())));
        }
        return lead;
    }

    @Transactional
    public Lead updateNotes(UUID businessId, UUID actorUserId, UUID leadId, String notes) {
        Lead lead = get(businessId, leadId);
        lead.updateNotes(notes);
        audit.record(AuditEvent.byUser(businessId, actorUserId, "lead.notes_updated", "Lead", leadId, null));
        return lead;
    }

    @Transactional
    public Lead saveQuoteDraft(UUID businessId, UUID actorUserId, ActorType actorType, UUID leadId, String quote) {
        Lead lead = get(businessId, leadId);
        lead.saveQuoteDraft(quote);
        audit.record(new AuditEvent(businessId, actorUserId, actorType, "lead.quote_drafted", "Lead",
                leadId.toString(), null));
        return lead;
    }

    @Transactional(readOnly = true)
    public LeadSummary summary(UUID businessId) {
        Map<LeadStatus, Long> byStatus = new EnumMap<>(LeadStatus.class);
        for (LeadStatus s : LeadStatus.values()) {
            byStatus.put(s, 0L);
        }
        leads.countByStatus(businessId).forEach(c -> byStatus.put(c.getStatus(), c.getCount()));
        long total = byStatus.values().stream().mapToLong(Long::longValue).sum();
        long recent = leads.countByBusinessIdAndCreatedAtAfter(businessId, clock.instant().minus(Duration.ofDays(7)));
        return new LeadSummary(total, recent, byStatus);
    }
}
