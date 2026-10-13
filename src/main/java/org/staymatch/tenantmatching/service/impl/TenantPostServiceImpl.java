package org.staymatch.tenantmatching.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.staymatch.common.exception.ResourceNotFoundException;
import org.staymatch.tenantmatching.dto.CreateTenantPostRequest;
import org.staymatch.tenantmatching.dto.TenantPostResponse;
import org.staymatch.tenantmatching.dto.TenantPostSearchCriteria;
import org.staymatch.tenantmatching.mapper.TenantPostMapper;
import org.staymatch.tenantmatching.mapper.UserMapper;
import org.staymatch.tenantmatching.model.Preference;
import org.staymatch.tenantmatching.model.TenantPost;
import org.staymatch.tenantmatching.model.User;
import org.staymatch.tenantmatching.repository.TenantPostRepository;
import org.staymatch.tenantmatching.repository.UserRepository;
import org.staymatch.tenantmatching.service.TenantPostService;
import org.staymatch.common.util.GeoUtils;

@Service
public class TenantPostServiceImpl implements TenantPostService {

    private final TenantPostRepository tenantPostRepository;
    private final UserRepository userRepository;
    private final TenantPostMapper tenantPostMapper;
    private final UserMapper userMapper;

    public TenantPostServiceImpl(
        TenantPostRepository tenantPostRepository,
        UserRepository userRepository,
        TenantPostMapper tenantPostMapper,
        UserMapper userMapper
    ) {
        this.tenantPostRepository = tenantPostRepository;
        this.userRepository = userRepository;
        this.tenantPostMapper = tenantPostMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public TenantPostResponse createTenantPost(CreateTenantPostRequest request) {
        validateBudgetRange(request.getMinBudget(), request.getMaxBudget());
        validateCapacity(request.getTotalCapacity(), request.getCurrentOccupancy());

        User user = userRepository.findByEmailIgnoreCase(request.getUser().getEmail())
            .orElseGet(() -> userMapper.toEntity(request.getUser()));

        user.setFullName(request.getUser().getFullName());
        user.setPhoneNumber(request.getUser().getPhoneNumber());
        user = userRepository.save(user);

        TenantPost tenantPost = tenantPostMapper.toEntity(request, user);
        TenantPost savedPost = tenantPostRepository.save(tenantPost);
        return tenantPostMapper.toResponse(savedPost);
    }

    @Override
    @Transactional(readOnly = true)
    public TenantPostResponse getTenantPostById(Long id) {
        TenantPost tenantPost = tenantPostRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant post not found with id: " + id));
        return tenantPostMapper.toResponse(tenantPost);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TenantPostResponse> searchTenantPosts(TenantPostSearchCriteria criteria) {
        List<TenantPost> posts = tenantPostRepository.findByIsActiveTrue();
        List<TenantPostResponse> results = new ArrayList<>();

        boolean hasCityFilter = criteria.getCity() != null && !criteria.getCity().isBlank();
        boolean hasAreaFilter = criteria.getArea() != null && !criteria.getArea().isBlank();

        for (TenantPost post : posts) {
            // Only show posts with available spots
            if (post.getTotalCapacity() - post.getCurrentOccupancy() <= 0) {
                continue;
            }

            // City-based filtering (exact match on addressCity)
            if (hasCityFilter && (post.getAddressCity() == null
                    || !post.getAddressCity().equalsIgnoreCase(criteria.getCity()))) {
                continue;
            }

            // Area-based filtering (partial match on shortAddress)
            if (hasAreaFilter && (post.getShortAddress() == null
                    || !post.getShortAddress().toLowerCase().contains(criteria.getArea().toLowerCase()))) {
                continue;
            }

            if (!matchesBudget(post, criteria.getBudget())) {
                continue;
            }
            if (!matchesPreferences(post.getPreference(), criteria)) {
                continue;
            }

            Double distanceKm = null;
            if (criteria.getLatitude() != null && criteria.getLongitude() != null) {
                distanceKm = GeoUtils.distanceKm(
                    criteria.getLatitude(),
                    criteria.getLongitude(),
                    post.getPreferredLatitude(),
                    post.getPreferredLongitude()
                );
                // Only apply radius filtering if no city/area filter is active
                if (!hasCityFilter && !hasAreaFilter
                        && criteria.getRadiusKm() != null && distanceKm > criteria.getRadiusKm()) {
                    continue;
                }
            }

            TenantPostResponse response = tenantPostMapper.toResponse(post);
            response.setDistanceKm(round(distanceKm));
            results.add(response);
        }

        applySorting(results, criteria.getSortBy());
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TenantPostResponse> findMatches(Long tenantPostId, Double radiusKm) {
        TenantPost sourcePost = tenantPostRepository.findById(tenantPostId)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant post not found with id: " + tenantPostId));

        List<TenantPost> candidates = tenantPostRepository.findByIsActiveTrueAndIdNot(tenantPostId);
        List<TenantPostResponse> matches = new ArrayList<>();

        for (TenantPost candidate : candidates) {
            Double distanceKm2 = GeoUtils.distanceKm(
                sourcePost.getPreferredLatitude(),
                sourcePost.getPreferredLongitude(),
                candidate.getPreferredLatitude(),
                candidate.getPreferredLongitude()
            );

            if (radiusKm != null && distanceKm2 > radiusKm) {
                continue;
            }

            double score = calculateMatchScore(sourcePost, candidate, distanceKm2, radiusKm);
            if (score <= 0) {
                continue;
            }

            TenantPostResponse response = tenantPostMapper.toResponse(candidate);
            response.setDistanceKm(round(distanceKm2));
            response.setMatchScore(round(score));
            matches.add(response);
        }

        matches.sort(Comparator.comparing(TenantPostResponse::getMatchScore).reversed());
        return matches;
    }

    @Override
    @Transactional
    public void deleteTenantPost(Long id) {
        TenantPost tenantPost = tenantPostRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant post not found with id: " + id));
        tenantPostRepository.delete(tenantPost);
    }

    private void validateBudgetRange(BigDecimal minBudget, BigDecimal maxBudget) {
        if (minBudget.compareTo(maxBudget) > 0) {
            throw new IllegalArgumentException("Minimum budget cannot be greater than maximum budget");
        }
    }

    private void validateCapacity(Integer totalCapacity, Integer currentOccupancy) {
        if (currentOccupancy > totalCapacity) {
            throw new IllegalArgumentException("Current occupancy cannot exceed total capacity");
        }
    }

    /**
     * Applies dynamic sorting to the results based on the sortBy string.
     * Format: "field,direction" e.g. "price,asc", "createdAt,desc".
     * Defaults to createdAt DESC (latest first).
     */
    private void applySorting(List<TenantPostResponse> results, String sortBy) {
        Comparator<TenantPostResponse> comparator;

        if (sortBy != null && !sortBy.isBlank()) {
            String[] parts = sortBy.split(",");
            String field = parts[0].trim();
            boolean ascending = parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim());

            comparator = switch (field.toLowerCase()) {
                case "price", "minbudget", "budget" ->
                    Comparator.comparing(TenantPostResponse::getMinBudget, Comparator.nullsLast(Comparator.naturalOrder()));
                default -> // "createdAt" and any unknown field
                    Comparator.comparing(TenantPostResponse::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
            };

            if (!ascending) {
                comparator = comparator.reversed();
            }
        } else {
            // Default: latest first
            comparator = Comparator.comparing(TenantPostResponse::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed();
        }

        results.sort(comparator);
    }

    private boolean matchesBudget(TenantPost post, BigDecimal budget) {
        if (budget == null) {
            return true;
        }
        return post.getMinBudget().compareTo(budget) <= 0 && post.getMaxBudget().compareTo(budget) >= 0;
    }

    private boolean matchesPreferences(Preference preference, TenantPostSearchCriteria criteria) {
        if (criteria.getFoodPreference() != null && !criteria.getFoodPreference().equalsIgnoreCase(preference.getFoodPreference())) {
            return false;
        }
        if (criteria.getSmokingPreference() != null && !criteria.getSmokingPreference().equalsIgnoreCase(preference.getSmokingPreference())) {
            return false;
        }
        return criteria.getPreferredGender() == null
            || criteria.getPreferredGender().equalsIgnoreCase(preference.getPreferredGender());
    }

    private double calculateMatchScore(TenantPost source, TenantPost candidate, double distanceKm, Double radiusKm) {
        double budgetScore = budgetCompatibility(source, candidate) * 40;
        double preferenceScore = preferenceCompatibility(source.getPreference(), candidate.getPreference()) * 30;

        double effectiveRadius = radiusKm != null && radiusKm > 0 ? radiusKm : 25.0;
        double normalizedDistance = Math.max(0, 1 - (distanceKm / effectiveRadius));
        double locationScore = normalizedDistance * 30;

        return Math.max(0, budgetScore + preferenceScore + locationScore);
    }

    private double budgetCompatibility(TenantPost a, TenantPost b) {
        BigDecimal overlapMin = a.getMinBudget().max(b.getMinBudget());
        BigDecimal overlapMax = a.getMaxBudget().min(b.getMaxBudget());
        if (overlapMin.compareTo(overlapMax) > 0) {
            return 0;
        }

        BigDecimal unionMin = a.getMinBudget().min(b.getMinBudget());
        BigDecimal unionMax = a.getMaxBudget().max(b.getMaxBudget());
        BigDecimal overlapRange = overlapMax.subtract(overlapMin);
        BigDecimal unionRange = unionMax.subtract(unionMin);

        if (unionRange.compareTo(BigDecimal.ZERO) == 0) {
            return 1;
        }
        return overlapRange.divide(unionRange, 4, RoundingMode.HALF_UP).doubleValue();
    }

    private double preferenceCompatibility(Preference source, Preference candidate) {
        int points = 0;
        int total = 3;

        if (equalsIgnoreCase(source.getFoodPreference(), candidate.getFoodPreference())) {
            points++;
        }
        if (equalsIgnoreCase(source.getSmokingPreference(), candidate.getSmokingPreference())) {
            points++;
        }
        if (equalsIgnoreCase(source.getPreferredGender(), candidate.getPreferredGender())) {
            points++;
        }

        return (double) points / total;
    }

    private boolean equalsIgnoreCase(String left, String right) {
        return left != null && right != null && left.equalsIgnoreCase(right);
    }

    private Double round(Double value) {
        if (value == null) {
            return null;
        }
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}

