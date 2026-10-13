package org.staymatch.pgdiscovery.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.staymatch.pgdiscovery.model.OwnerProfile;

public interface OwnerProfileRepository extends JpaRepository<OwnerProfile, Long> {

    Optional<OwnerProfile> findByEmailIgnoreCase(String email);
}

