package org.staymatch.tenantmatching.repository;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.staymatch.tenantmatching.model.TenantPost;

public interface TenantPostRepository extends JpaRepository<TenantPost, Long> {

    @EntityGraph(attributePaths = {"user", "preference"})
    List<TenantPost> findByIsActiveTrue();

    @EntityGraph(attributePaths = {"user", "preference"})
    List<TenantPost> findByIsActiveTrueAndIdNot(Long id);
}
