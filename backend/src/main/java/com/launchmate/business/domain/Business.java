package com.launchmate.business.domain;

import com.launchmate.shared.persistence.TimestampedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

/** Tenant root. Every tenant-owned record references a business id. */
@Entity
@Table(name = "businesses")
public class Business extends TimestampedEntity {

    @Id
    private UUID id;

    @Column(nullable = false, updatable = false)
    private String slug;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BusinessCategory category;

    private String tagline;
    private String description;
    private String city;
    private String area;
    private String address;
    private String phone;
    private String whatsapp;
    private String email;

    @Column(name = "target_customers")
    private String targetCustomers;

    private String languages;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "opening_hours")
    private List<DailyHours> openingHours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Plan plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "setup_stage", nullable = false)
    private SetupStage setupStage;

    protected Business() {
    }

    public Business(String slug, BusinessProfile profile) {
        this.id = UUID.randomUUID();
        this.slug = slug;
        this.plan = Plan.FREE;
        this.setupStage = SetupStage.PROFILE_DRAFT;
        applyProfile(profile);
    }

    public void applyProfile(BusinessProfile p) {
        this.name = p.name().trim();
        this.category = p.category();
        this.tagline = trimToNull(p.tagline());
        this.description = trimToNull(p.description());
        this.city = trimToNull(p.city());
        this.area = trimToNull(p.area());
        this.address = trimToNull(p.address());
        this.phone = trimToNull(p.phone());
        this.whatsapp = trimToNull(p.whatsapp());
        this.email = trimToNull(p.email());
        this.targetCustomers = trimToNull(p.targetCustomers());
        this.languages = trimToNull(p.languages());
        this.openingHours = p.openingHours() == null ? null : List.copyOf(p.openingHours());
    }

    public void replaceOpeningHours(List<DailyHours> hours) {
        this.openingHours = List.copyOf(hours);
    }

    /** Advances the onboarding stage; never moves backwards. */
    public void advanceTo(SetupStage stage) {
        if (setupStage.isBefore(stage)) {
            this.setupStage = stage;
        }
    }

    public BusinessProfile toProfile() {
        return new BusinessProfile(name, category, tagline, description, city, area, address, phone, whatsapp,
                email, targetCustomers, languages, openingHours);
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    public UUID getId() { return id; }
    public String getSlug() { return slug; }
    public String getName() { return name; }
    public BusinessCategory getCategory() { return category; }
    public String getTagline() { return tagline; }
    public String getDescription() { return description; }
    public String getCity() { return city; }
    public String getArea() { return area; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public String getWhatsapp() { return whatsapp; }
    public String getEmail() { return email; }
    public String getTargetCustomers() { return targetCustomers; }
    public String getLanguages() { return languages; }
    public List<DailyHours> getOpeningHours() { return openingHours; }
    public Plan getPlan() { return plan; }
    public SetupStage getSetupStage() { return setupStage; }
}
