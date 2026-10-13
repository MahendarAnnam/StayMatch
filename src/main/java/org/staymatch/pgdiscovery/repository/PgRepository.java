package org.staymatch.pgdiscovery.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.staymatch.pgdiscovery.model.Pg;

public interface PgRepository extends JpaRepository<Pg, Long>, JpaSpecificationExecutor<Pg> {

    /**
     * Eager-fetch all associations for a single PG lookup — eliminates N+1 on detail view.
     */
    @Override
    @EntityGraph(attributePaths = {"owner", "sharingOptions", "pgFacilities.facility", "images"})
    Optional<Pg> findById(Long id);

    /**
     * Eager-fetch all associations for paginated search results — eliminates N+1 on listing.
     */
    @EntityGraph(attributePaths = {"owner", "sharingOptions", "pgFacilities.facility", "images"})
    Page<Pg> findAll(Specification<Pg> spec, Pageable pageable);
}

