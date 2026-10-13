package org.staymatch.pgdiscovery.service.impl;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.staymatch.common.exception.ResourceNotFoundException;
import org.staymatch.pgdiscovery.dto.CreatePgRequest;
import org.staymatch.pgdiscovery.dto.PgResponse;
import org.staymatch.pgdiscovery.dto.UpdatePgRequest;
import org.staymatch.pgdiscovery.mapper.PgMapper;
import org.staymatch.pgdiscovery.model.Facility;
import org.staymatch.pgdiscovery.model.OwnerProfile;
import org.staymatch.pgdiscovery.model.Pg;
import org.staymatch.pgdiscovery.model.PgFacility;
import org.staymatch.pgdiscovery.repository.FacilityRepository;
import org.staymatch.pgdiscovery.repository.OwnerProfileRepository;
import org.staymatch.pgdiscovery.repository.PgRepository;
import org.staymatch.pgdiscovery.service.PgService;

@Service
public class PgServiceImpl implements PgService {

    private static final Logger log = LoggerFactory.getLogger(PgServiceImpl.class);

    private final PgRepository pgRepository;
    private final OwnerProfileRepository ownerProfileRepository;
    private final FacilityRepository facilityRepository;
    private final PgMapper pgMapper;

    public PgServiceImpl(PgRepository pgRepository,
                         OwnerProfileRepository ownerProfileRepository,
                         FacilityRepository facilityRepository,
                         PgMapper pgMapper) {
        this.pgRepository = pgRepository;
        this.ownerProfileRepository = ownerProfileRepository;
        this.facilityRepository = facilityRepository;
        this.pgMapper = pgMapper;
    }

    @Override
    @Transactional
    @CacheEvict(value = "pg_search", allEntries = true)
    public PgResponse createPg(CreatePgRequest request) {
        log.info("Creating PG '{}' for owner '{}'", request.getName(), request.getOwner().getEmail());
        OwnerProfile owner = resolveOwner(request);
        Pg pg = pgMapper.toEntity(request, owner);
        attachFacilities(pg, request.getFacilityNames());
        Pg savedPg = pgRepository.save(pg);
        log.info("PG created successfully with id={}", savedPg.getId());
        return pgMapper.toResponse(savedPg);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "pg_details", key = "'pg:' + #id")
    public PgResponse getPgById(Long id) {
        log.debug("Fetching PG by id={}", id);
        Pg pg = pgRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("PG not found with id={}", id);
                return new ResourceNotFoundException("PG not found with id: " + id);
            });
        return pgMapper.toResponse(pg);
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "pg_search", allEntries = true),
        @CacheEvict(value = "pg_details", allEntries = true)
    })
    public PgResponse updatePg(Long id, UpdatePgRequest request) {
        log.info("Updating PG with id={}", id);
        Pg pg = pgRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("PG not found with id={}", id);
                return new ResourceNotFoundException("PG not found with id: " + id);
            });

        validateOwnership(pg);

        pgMapper.updateEntity(pg, request);
        attachFacilities(pg, request.getFacilityNames());

        Pg updatedPg = pgRepository.save(pg);
        log.info("PG updated successfully with id={}", updatedPg.getId());
        return pgMapper.toResponse(updatedPg);
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "pg_search", allEntries = true),
        @CacheEvict(value = "pg_details", allEntries = true)
    })
    public void deletePg(Long id) {
        log.info("Deleting PG with id={}", id);
        Pg pg = pgRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("PG not found with id={}", id);
                return new ResourceNotFoundException("PG not found with id: " + id);
            });

        validateOwnership(pg);

        pgRepository.delete(pg);
        log.info("PG deleted successfully with id={}", id);
    }

    // --- Private helpers ---

    /**
     * Verifies that the currently authenticated user is the owner of the given PG.
     * Throws AccessDeniedException if the user is not the owner.
     */
    private void validateOwnership(Pg pg) {
        String authenticatedEmail = getAuthenticatedEmail();
        String ownerEmail = pg.getOwner().getEmail();

        if (!ownerEmail.equalsIgnoreCase(authenticatedEmail)) {
            log.warn("Ownership check failed: authenticated user '{}' is not the owner of PG id={}",
                authenticatedEmail, pg.getId());
            throw new AccessDeniedException(
                "You are not the owner of this PG and cannot modify it");
        }
    }

    /**
     * Extracts the authenticated user's email from the SecurityContext.
     */
    private String getAuthenticatedEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        return authentication.getName();
    }

    private OwnerProfile resolveOwner(CreatePgRequest request) {
        String email = request.getOwner().getEmail().toLowerCase();
        OwnerProfile owner = ownerProfileRepository.findByEmailIgnoreCase(email)
            .orElseGet(() -> {
                log.debug("Owner not found for email='{}', creating new profile", email);
                OwnerProfile newOwner = new OwnerProfile();
                newOwner.setEmail(email);
                return newOwner;
            });

        owner.setFullName(request.getOwner().getFullName());
        owner.setPhoneNumber(request.getOwner().getPhoneNumber());
        return ownerProfileRepository.save(owner);
    }

    private void attachFacilities(Pg pg, List<String> facilityNames) {
        if (facilityNames == null || facilityNames.isEmpty()) {
            return;
        }

        log.debug("Attaching {} facilities to PG", facilityNames.size());
        for (String name : facilityNames) {
            Facility facility = facilityRepository
                .findByFacilityNameIgnoreCase(name.trim())
                .orElseGet(() -> {
                    log.debug("Facility '{}' not found, creating new entry", name.trim());
                    Facility newFacility = new Facility();
                    newFacility.setFacilityName(name.trim());
                    return facilityRepository.save(newFacility);
                });

            PgFacility pgFacility = new PgFacility();
            pgFacility.setFacility(facility);
            pg.addPgFacility(pgFacility);
        }
    }
}
