package org.staymatch.tenantmatching.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.staymatch.tenantmatching.dto.CreateTenantPostRequest;
import org.staymatch.tenantmatching.dto.TenantPostResponse;
import org.staymatch.tenantmatching.dto.TenantPostSearchCriteria;
import org.staymatch.tenantmatching.service.TenantPostService;

@Validated
@RestController
@RequestMapping("/api/v1/tenant-posts")
public class TenantPostController {

    private final TenantPostService tenantPostService;

    public TenantPostController(TenantPostService tenantPostService) {
        this.tenantPostService = tenantPostService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TENANT', 'ADMIN')")
    public ResponseEntity<TenantPostResponse> createTenantPost(@Valid @RequestBody CreateTenantPostRequest request) {
        TenantPostResponse response = tenantPostService.createTenantPost(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TenantPostResponse> getTenantPostById(@PathVariable @Min(1) Long id) {
        return ResponseEntity.ok(tenantPostService.getTenantPostById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<TenantPostResponse>> searchTenantPosts(
        @RequestParam(required = false) @DecimalMin(value = "0.0", inclusive = false) BigDecimal budget,
        @RequestParam(required = false) Double latitude,
        @RequestParam(required = false) Double longitude,
        @RequestParam(required = false) @DecimalMin(value = "0.1", inclusive = true) Double radiusKm,
        @RequestParam(required = false) String city,
        @RequestParam(required = false) String area,
        @RequestParam(required = false) String foodPreference,
        @RequestParam(required = false) String smokingPreference,
        @RequestParam(required = false) String preferredGender,
        @RequestParam(required = false, defaultValue = "createdAt,desc") String sortBy
    ) {
        TenantPostSearchCriteria criteria = new TenantPostSearchCriteria();
        criteria.setBudget(budget);
        criteria.setLatitude(latitude);
        criteria.setLongitude(longitude);
        criteria.setRadiusKm(radiusKm);
        criteria.setCity(city);
        criteria.setArea(area);
        criteria.setFoodPreference(foodPreference);
        criteria.setSmokingPreference(smokingPreference);
        criteria.setPreferredGender(preferredGender);
        criteria.setSortBy(sortBy);

        return ResponseEntity.ok(tenantPostService.searchTenantPosts(criteria));
    }

    @GetMapping("/{id}/matches")
    public ResponseEntity<List<TenantPostResponse>> findMatches(
        @PathVariable("id") @Min(1) Long tenantPostId,
        @RequestParam(required = false) @DecimalMin(value = "0.1", inclusive = true) Double radiusKm
    ) {
        return ResponseEntity.ok(tenantPostService.findMatches(tenantPostId, radiusKm));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT', 'ADMIN')")
    public ResponseEntity<Void> deleteTenantPost(@PathVariable @Min(1) Long id) {
        tenantPostService.deleteTenantPost(id);
        return ResponseEntity.noContent().build();
    }
}
