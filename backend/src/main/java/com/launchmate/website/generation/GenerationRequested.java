package com.launchmate.website.generation;

import java.util.UUID;

/** Published inside the request transaction; the runner starts only after it commits. */
record GenerationRequested(UUID jobId, UUID businessId, UUID userId) {
}
