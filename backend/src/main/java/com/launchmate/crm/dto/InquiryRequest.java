package com.launchmate.crm.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

/**
 * A customer inquiry from a public website (or entered manually by the owner).
 *
 * @param website honeypot field — real visitors never see or fill it
 */
public record InquiryRequest(
        @NotBlank @Size(max = 120) String name,
        @Pattern(regexp = "^[+0-9 ()-]{6,20}$", message = "is not a valid phone number") String phone,
        @Email @Size(max = 254) String email,
        @NotBlank @Size(max = 2000) String message,
        UUID productId,
        @FutureOrPresent LocalDate preferredDate,
        @Size(max = 60) String budget,
        @Size(max = 200) String website) {

    @JsonIgnore
    @AssertTrue(message = "Please give a phone number or an email so the business can reply")
    public boolean isContactable() {
        return (phone != null && !phone.isBlank()) || (email != null && !email.isBlank());
    }
}
