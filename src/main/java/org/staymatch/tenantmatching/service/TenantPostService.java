package org.staymatch.tenantmatching.service;

import java.util.List;
import org.staymatch.tenantmatching.dto.CreateTenantPostRequest;
import org.staymatch.tenantmatching.dto.TenantPostResponse;
import org.staymatch.tenantmatching.dto.TenantPostSearchCriteria;

public interface TenantPostService {

    TenantPostResponse createTenantPost(CreateTenantPostRequest request);

    TenantPostResponse getTenantPostById(Long id);

    List<TenantPostResponse> searchTenantPosts(TenantPostSearchCriteria criteria);

    List<TenantPostResponse> findMatches(Long tenantPostId, Double radiusKm);

    void deleteTenantPost(Long id);
}

