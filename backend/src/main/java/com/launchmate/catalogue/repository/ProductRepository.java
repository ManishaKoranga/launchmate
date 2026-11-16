package com.launchmate.catalogue.repository;

import com.launchmate.catalogue.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Every query is constrained by business id — there is deliberately no tenant-agnostic finder in use. */
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findByIdAndBusinessId(UUID id, UUID businessId);

    Page<Product> findByBusinessId(UUID businessId, Pageable pageable);

    List<Product> findByBusinessIdAndVisibleTrueOrderBySortOrderAscCreatedAtAsc(UUID businessId);

    long countByBusinessId(UUID businessId);

    @Query("select coalesce(max(p.sortOrder), 0) from Product p where p.businessId = :businessId")
    int maxSortOrder(@Param("businessId") UUID businessId);
}
