package com.launchmate.website.generation;

public record JobStep(String key, String label, StepStatus status) {

    public enum StepStatus { PENDING, RUNNING, DONE, FAILED }

    JobStep with(StepStatus newStatus) {
        return new JobStep(key, label, newStatus);
    }
}
