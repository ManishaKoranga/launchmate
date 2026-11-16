package com.launchmate.business.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.DayOfWeek;
import java.time.LocalTime;

/** Opening hours for one weekday. Times are local "HH:mm"; {@code closed} days carry no times. */
public record DailyHours(
        @NotNull DayOfWeek day,
        boolean closed,
        @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "must be HH:mm") String opens,
        @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "must be HH:mm") String closes) {

    @JsonIgnore // validation-only; must not be serialised into JSONB or API responses
    @AssertTrue(message = "open days need opening and closing times, with closing after opening")
    public boolean isConsistent() {
        if (closed) {
            return true;
        }
        if (opens == null || closes == null || !opens.matches("\\d\\d:\\d\\d") || !closes.matches("\\d\\d:\\d\\d")) {
            return false;
        }
        return LocalTime.parse(closes).isAfter(LocalTime.parse(opens));
    }
}
