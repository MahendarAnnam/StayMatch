package org.staymatch.pgdiscovery.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.staymatch.pgdiscovery.dto.PgResponse;
import org.staymatch.pgdiscovery.dto.PgSearchCriteria;

/**
 * Handles PG search, filtering, and geo-based discovery.
 * Separated from PgService following Single Responsibility Principle.
 */
public interface PgSearchService {

    Page<PgResponse> searchPgs(PgSearchCriteria criteria, Pageable pageable);
}

