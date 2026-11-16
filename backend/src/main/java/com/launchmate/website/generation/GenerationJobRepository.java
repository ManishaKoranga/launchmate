package com.launchmate.website.generation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface GenerationJobRepository extends JpaRepository<GenerationJob, UUID> {

    Optional<GenerationJob> findByIdAndBusinessId(UUID id, UUID businessId);

    List<GenerationJob> findTop5ByBusinessIdOrderByCreatedAtDesc(UUID businessId);
}
