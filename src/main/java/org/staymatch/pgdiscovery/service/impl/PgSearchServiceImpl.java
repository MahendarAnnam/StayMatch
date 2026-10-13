package org.staymatch.pgdiscovery.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.staymatch.common.util.GeoUtils;
import org.staymatch.pgdiscovery.dto.PgResponse;
import org.staymatch.pgdiscovery.dto.PgSearchCriteria;
import org.staymatch.pgdiscovery.mapper.PgMapper;
import org.staymatch.pgdiscovery.model.FoodType;
import org.staymatch.pgdiscovery.model.Pg;
import org.staymatch.pgdiscovery.repository.PgRepository;
import org.staymatch.pgdiscovery.service.PgSearchService;
import org.staymatch.pgdiscovery.specification.PgSpecifications;

@Service
public class PgSearchServiceImpl implements PgSearchService {

    private static final Logger log = LoggerFactory.getLogger(PgSearchServiceImpl.class);

    private final PgRepository pgRepository;
    private final PgMapper pgMapper;

    public PgSearchServiceImpl(PgRepository pgRepository, PgMapper pgMapper) {
        this.pgRepository = pgRepository;
        this.pgMapper = pgMapper;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "pg_search",
        key = "'search:' + #criteria.latitude + ':' + #criteria.longitude + ':' + #criteria.radiusKm"
            + " + ':' + #criteria.minPrice + ':' + #criteria.maxPrice"
            + " + ':' + #criteria.sharingType + ':' + #criteria.facilityNames"
            + " + ':' + #criteria.foodType"
            + " + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<PgResponse> searchPgs(PgSearchCriteria criteria, Pageable pageable) {
        validatePriceRange(criteria);

        boolean hasGeoFilter = isGeoFilterActive(criteria);
        boolean hasCityOrAreaFilter = (criteria.getCity() != null && !criteria.getCity().isBlank())
                                   || (criteria.getArea() != null && !criteria.getArea().isBlank());

        log.info("Searching PGs: geoFilter={}, city={}, area={}, sharingType={}, priceRange=[{}-{}], page={}",
            hasGeoFilter, criteria.getCity(), criteria.getArea(),
            criteria.getSharingType(), criteria.getMinPrice(), criteria.getMaxPrice(), pageable.getPageNumber());

        Specification<Pg> spec = buildSpecification(criteria, hasGeoFilter);
        Page<Pg> pgPage = pgRepository.findAll(spec, pageable);
        log.debug("Specification query returned {} PGs", pgPage.getTotalElements());

        // CASE 1: City/area filter active — no radius filtering needed, just compute distances
        if (hasCityOrAreaFilter) {
            return pgPage.map(pg -> {
                PgResponse response = pgMapper.toResponse(pg);
                response.setDistanceKm(computeDistance(criteria, pg));
                return response;
            });
        }

        if (!hasGeoFilter) {
            // No geo filtering — map straight through, compute distance if coords provided
            return pgPage.map(pg -> {
                PgResponse response = pgMapper.toResponse(pg);
                response.setDistanceKm(computeDistance(criteria, pg));
                return response;
            });
        }

        // Bounding box already narrowed the DB results.
        // Now apply precise Haversine to exclude corner cases outside the circular radius.
        List<PgResponse> filtered = pgPage.getContent().stream()
            .map(pg -> {
                double distance = GeoUtils.distanceKm(
                    criteria.getLatitude(),
                    criteria.getLongitude(),
                    pg.getLatitude(),
                    pg.getLongitude()
                );

                if (distance > criteria.getRadiusKm()) {
                    return null; // inside bounding box but outside circular radius
                }

                PgResponse response = pgMapper.toResponse(pg);
                response.setDistanceKm(round(distance));
                return response;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        log.debug("After Haversine filtering: {} PGs within {}km radius", filtered.size(), criteria.getRadiusKm());
        return new PageImpl<>(filtered, pageable, filtered.size());
    }

    // --- Private helpers ---

    private void validatePriceRange(PgSearchCriteria criteria) {
        if (criteria.getMinPrice() != null && criteria.getMaxPrice() != null
            && criteria.getMinPrice().compareTo(criteria.getMaxPrice()) > 0) {
            throw new IllegalArgumentException("minPrice cannot be greater than maxPrice");
        }
    }

    private boolean isGeoFilterActive(PgSearchCriteria criteria) {
        return criteria.getLatitude() != null
            && criteria.getLongitude() != null
            && criteria.getRadiusKm() != null;
    }

    private Specification<Pg> buildSpecification(PgSearchCriteria criteria, boolean applyBoundingBox) {
        Specification<Pg> spec = Specification.where(PgSpecifications.isActive());

        // City-based filtering (exact match) — takes priority over bounding box
        boolean hasCityFilter = criteria.getCity() != null && !criteria.getCity().isBlank();
        boolean hasAreaFilter = criteria.getArea() != null && !criteria.getArea().isBlank();

        if (hasCityFilter) {
            spec = spec.and(PgSpecifications.inCity(criteria.getCity()));
        }

        if (hasAreaFilter) {
            spec = spec.and(PgSpecifications.inArea(criteria.getArea()));
        }

        // Bounding-box pre-filter — only if no city/area filter and geo coords are present
        if (!hasCityFilter && !hasAreaFilter && applyBoundingBox) {
            double[] box = GeoUtils.boundingBox(
                criteria.getLatitude(),
                criteria.getLongitude(),
                criteria.getRadiusKm()
            );
            spec = spec.and(PgSpecifications.withinBoundingBox(box[0], box[1], box[2], box[3]));
        }

        if (criteria.getSharingType() != null && !criteria.getSharingType().isBlank()) {
            spec = spec.and(PgSpecifications.hasSharingType(criteria.getSharingType()));
        }

        if (criteria.getMinPrice() != null || criteria.getMaxPrice() != null) {
            spec = spec.and(PgSpecifications.rentBetween(criteria.getMinPrice(), criteria.getMaxPrice()));
        }

        if (criteria.getFacilityNames() != null && !criteria.getFacilityNames().isEmpty()) {
            spec = spec.and(PgSpecifications.hasFacilities(criteria.getFacilityNames()));
        }

        if (criteria.getFoodType() != null && !criteria.getFoodType().isBlank()) {
            try {
                FoodType ft = FoodType.valueOf(criteria.getFoodType().toUpperCase());
                spec = spec.and(PgSpecifications.hasFoodType(ft));
            } catch (IllegalArgumentException ignored) {
                log.warn("Invalid foodType filter value: {}", criteria.getFoodType());
            }
        }

        return spec;
    }

    private Double computeDistance(PgSearchCriteria criteria, Pg pg) {
        if (criteria.getLatitude() == null || criteria.getLongitude() == null) {
            return null;
        }

        return round(GeoUtils.distanceKm(
            criteria.getLatitude(),
            criteria.getLongitude(),
            pg.getLatitude(),
            pg.getLongitude()
        ));
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
