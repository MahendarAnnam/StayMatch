package org.staymatch.pgdiscovery.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.staymatch.pgdiscovery.model.Facility;
import org.staymatch.pgdiscovery.model.FoodType;
import org.staymatch.pgdiscovery.model.Pg;
import org.staymatch.pgdiscovery.model.PgFacility;
import org.staymatch.pgdiscovery.model.SharingOption;

/**
 * Builds JPA Specification predicates for the Pg entity.
 * Each static method returns a Specification that can be combined with {@code Specification.where(...).and(...)}.
 */
public final class PgSpecifications {

    private PgSpecifications() {
    }

    public static Specification<Pg> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("isActive"));
    }

    /**
     * Filters PGs by city (case-insensitive exact match on addressCity column).
     */
    public static Specification<Pg> inCity(String city) {
        return (root, query, cb) -> cb.equal(cb.lower(root.get("addressCity")), city.toLowerCase());
    }

    /**
     * Filters PGs by area/locality using LIKE on shortAddress column.
     * Matches if shortAddress contains the area string (case-insensitive).
     */
    public static Specification<Pg> inArea(String area) {
        return (root, query, cb) -> cb.like(
            cb.lower(root.get("shortAddress")),
            "%" + area.toLowerCase() + "%"
        );
    }

    /**
     * Bounding-box pre-filter on indexed latitude/longitude columns.
     * Narrows the result set at DB level before precise Haversine filtering in-memory.
     */
    public static Specification<Pg> withinBoundingBox(double minLat, double maxLat, double minLng, double maxLng) {
        return (root, query, cb) -> cb.and(
            cb.between(root.get("latitude"), minLat, maxLat),
            cb.between(root.get("longitude"), minLng, maxLng)
        );
    }

    public static Specification<Pg> hasSharingType(String sharingType) {
        return (root, query, cb) -> {
            query.distinct(true);
            Join<Pg, SharingOption> join = root.join("sharingOptions", JoinType.INNER);
            return cb.equal(cb.lower(join.get("sharingType")), sharingType.toLowerCase());
        };
    }

    public static Specification<Pg> rentBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, cb) -> {
            query.distinct(true);
            Join<Pg, SharingOption> join = root.join("sharingOptions", JoinType.INNER);
            List<Predicate> predicates = new ArrayList<>();
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(join.get("rent"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(join.get("rent"), maxPrice));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Pg> hasFacilities(List<String> facilityNames) {
        return (root, query, cb) -> {
            query.distinct(true);
            Join<Pg, PgFacility> pgFacilityJoin = root.join("pgFacilities", JoinType.INNER);
            Join<PgFacility, Facility> facilityJoin = pgFacilityJoin.join("facility", JoinType.INNER);

            List<String> lowerNames = facilityNames.stream()
                .map(String::toLowerCase)
                .toList();

            return cb.lower(facilityJoin.get("facilityName")).in(lowerNames);
        };
    }

    /**
     * Filters PGs by food type. Also matches BOTH when searching for VEG or NON_VEG.
     */
    public static Specification<Pg> hasFoodType(FoodType foodType) {
        return (root, query, cb) -> {
            if (foodType == FoodType.BOTH) {
                return cb.equal(root.get("foodType"), FoodType.BOTH);
            }
            // If searching for VEG, match VEG or BOTH; same for NON_VEG
            return cb.or(
                cb.equal(root.get("foodType"), foodType),
                cb.equal(root.get("foodType"), FoodType.BOTH)
            );
        };
    }
}

