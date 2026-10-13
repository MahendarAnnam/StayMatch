package org.staymatch.pgdiscovery.service;

import org.staymatch.pgdiscovery.dto.CreatePgRequest;
import org.staymatch.pgdiscovery.dto.PgResponse;
import org.staymatch.pgdiscovery.dto.UpdatePgRequest;

/**
 * Handles PG lifecycle operations (create, read, update, delete).
 */
public interface PgService {

    PgResponse createPg(CreatePgRequest request);

    PgResponse getPgById(Long id);

    PgResponse updatePg(Long id, UpdatePgRequest request);

    void deletePg(Long id);
}

