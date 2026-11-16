package com.launchmate.website.repository;

import com.launchmate.website.domain.Website;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WebsiteRepository extends JpaRepository<Website, UUID> {

    Optional<Website> findByBusinessId(UUID businessId);
}
