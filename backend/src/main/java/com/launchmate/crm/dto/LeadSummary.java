package com.launchmate.crm.dto;

import com.launchmate.crm.domain.LeadStatus;

import java.util.Map;

public record LeadSummary(long total, long lastSevenDays, Map<LeadStatus, Long> byStatus) {
}
