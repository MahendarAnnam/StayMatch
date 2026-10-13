package org.staymatch.tenantmatching.mapper;

import org.springframework.stereotype.Component;
import org.staymatch.tenantmatching.dto.CreateTenantPostRequest;
import org.staymatch.tenantmatching.dto.TenantPostResponse;
import org.staymatch.tenantmatching.model.TenantPost;
import org.staymatch.tenantmatching.model.User;

@Component
public class TenantPostMapper {

    private final UserMapper userMapper;
    private final PreferenceMapper preferenceMapper;

    public TenantPostMapper(UserMapper userMapper, PreferenceMapper preferenceMapper) {
        this.userMapper = userMapper;
        this.preferenceMapper = preferenceMapper;
    }

    public TenantPost toEntity(CreateTenantPostRequest request, User user) {
        TenantPost tenantPost = new TenantPost();
        tenantPost.setUser(user);
        tenantPost.setTitle(request.getTitle());
        tenantPost.setDescription(request.getDescription());
        tenantPost.setMinBudget(request.getMinBudget());
        tenantPost.setMaxBudget(request.getMaxBudget());
        tenantPost.setPreferredLatitude(request.getPreferredLatitude());
        tenantPost.setPreferredLongitude(request.getPreferredLongitude());
        tenantPost.setLocationLabel(request.getLocationLabel());
        tenantPost.setFullAddress(request.getFullAddress());
        tenantPost.setShortAddress(request.getShortAddress());
        tenantPost.setAddressCity(request.getAddressCity());
        tenantPost.setAddressState(request.getAddressState());
        tenantPost.setPincode(request.getPincode());
        tenantPost.setMoveInDate(request.getMoveInDate());
        tenantPost.setTotalCapacity(request.getTotalCapacity());
        tenantPost.setCurrentOccupancy(request.getCurrentOccupancy());
        tenantPost.setPreference(preferenceMapper.toEntity(request.getPreference()));
        tenantPost.setIsActive(Boolean.TRUE);
        return tenantPost;
    }

    public TenantPostResponse toResponse(TenantPost tenantPost) {
        TenantPostResponse response = new TenantPostResponse();
        response.setId(tenantPost.getId());
        response.setUser(userMapper.toResponse(tenantPost.getUser()));
        response.setTitle(tenantPost.getTitle());
        response.setDescription(tenantPost.getDescription());
        response.setMinBudget(tenantPost.getMinBudget());
        response.setMaxBudget(tenantPost.getMaxBudget());
        response.setPreferredLatitude(tenantPost.getPreferredLatitude());
        response.setPreferredLongitude(tenantPost.getPreferredLongitude());
        response.setLocationLabel(tenantPost.getLocationLabel());
        response.setFullAddress(tenantPost.getFullAddress());
        response.setShortAddress(tenantPost.getShortAddress());
        response.setAddressCity(tenantPost.getAddressCity());
        response.setAddressState(tenantPost.getAddressState());
        response.setPincode(tenantPost.getPincode());
        response.setMoveInDate(tenantPost.getMoveInDate());
        response.setTotalCapacity(tenantPost.getTotalCapacity());
        response.setCurrentOccupancy(tenantPost.getCurrentOccupancy());
        response.setIsActive(tenantPost.getIsActive());
        response.setPreference(preferenceMapper.toResponse(tenantPost.getPreference()));
        response.setCreatedAt(tenantPost.getCreatedAt());
        response.setUpdatedAt(tenantPost.getUpdatedAt());
        return response;
    }
}

