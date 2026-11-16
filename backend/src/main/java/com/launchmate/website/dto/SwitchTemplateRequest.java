package com.launchmate.website.dto;

import jakarta.validation.constraints.NotBlank;

public record SwitchTemplateRequest(@NotBlank String templateId) {
}
