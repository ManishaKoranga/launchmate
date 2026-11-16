package com.launchmate.shared.exception;

import java.util.List;

/** A request that is well-formed but violates a business rule (HTTP 422). */
public class BusinessRuleException extends RuntimeException {

    private final List<String> problems;

    public BusinessRuleException(String message) {
        this(message, List.of());
    }

    public BusinessRuleException(String message, List<String> problems) {
        super(message);
        this.problems = List.copyOf(problems);
    }

    /** Individual problems, e.g. each failed website validation rule. */
    public List<String> getProblems() {
        return problems;
    }
}
