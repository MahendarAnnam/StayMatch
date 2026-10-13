package org.staymatch.tenantmatching.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.staymatch.tenantmatching.model.Preference;

public interface PreferenceRepository extends JpaRepository<Preference, Long> {
}

