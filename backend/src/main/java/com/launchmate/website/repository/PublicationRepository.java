package com.launchmate.website.repository;

import com.launchmate.website.domain.Publication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PublicationRepository extends JpaRepository<Publication, UUID> {

    Optional<Publication> findByIdAndBusinessId(UUID id, UUID businessId);

    @Query("select coalesce(max(p.version), 0) from Publication p where p.websiteId = :websiteId")
    int maxVersion(@Param("websiteId") UUID websiteId);
}
