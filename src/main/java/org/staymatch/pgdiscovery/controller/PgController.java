package org.staymatch.pgdiscovery.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.staymatch.pgdiscovery.dto.CreatePgRequest;
import org.staymatch.pgdiscovery.dto.PgResponse;
import org.staymatch.pgdiscovery.dto.PgSearchCriteria;
import org.staymatch.pgdiscovery.dto.UpdatePgRequest;
import org.staymatch.pgdiscovery.service.PgSearchService;
import org.staymatch.pgdiscovery.service.PgService;

@Validated
@RestController
@RequestMapping("/api/v1/pgs")
public class PgController {

    private final PgService pgService;
    private final PgSearchService pgSearchService;

    public PgController(PgService pgService, PgSearchService pgSearchService) {
        this.pgService = pgService;
        this.pgSearchService = pgSearchService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PG_OWNER', 'ADMIN')")
    public ResponseEntity<PgResponse> createPg(@Valid @RequestBody CreatePgRequest request) {
        PgResponse response = pgService.createPg(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PG_OWNER', 'ADMIN')")
    public ResponseEntity<PgResponse> updatePg(@PathVariable @Min(1) Long id,
                                                @Valid @RequestBody UpdatePgRequest request) {
        PgResponse response = pgService.updatePg(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PG_OWNER', 'ADMIN')")
    public ResponseEntity<Void> deletePg(@PathVariable @Min(1) Long id) {
        pgService.deletePg(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PgResponse> getPgById(@PathVariable @Min(1) Long id) {
        return ResponseEntity.ok(pgService.getPgById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<PgResponse>> searchPgs(
        @RequestParam(required = false) Double latitude,
        @RequestParam(required = false) Double longitude,
        @RequestParam(required = false) Double radiusKm,
        @RequestParam(required = false) String city,
        @RequestParam(required = false) String area,
        @RequestParam(required = false) BigDecimal minPrice,
        @RequestParam(required = false) BigDecimal maxPrice,
        @RequestParam(required = false) String sharingType,
        @RequestParam(required = false) List<String> facilityNames,
        @RequestParam(required = false) String foodType,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PgSearchCriteria criteria = new PgSearchCriteria();
        criteria.setLatitude(latitude);
        criteria.setLongitude(longitude);
        criteria.setRadiusKm(radiusKm);
        criteria.setCity(city);
        criteria.setArea(area);
        criteria.setMinPrice(minPrice);
        criteria.setMaxPrice(maxPrice);
        criteria.setSharingType(sharingType);
        criteria.setFacilityNames(facilityNames);
        criteria.setFoodType(foodType);

        return ResponseEntity.ok(pgSearchService.searchPgs(criteria, pageable));
    }
}
