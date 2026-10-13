package org.staymatch.pgdiscovery.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.staymatch.pgdiscovery.model.Facility;

public interface FacilityRepository extends JpaRepository<Facility, Long> {

    Optional<Facility> findByFacilityNameIgnoreCase(String facilityName);
}

