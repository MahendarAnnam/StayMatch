package org.staymatch.pgdiscovery.mapper;

import org.springframework.stereotype.Component;
import org.staymatch.pgdiscovery.dto.OwnerProfileRequest;
import org.staymatch.pgdiscovery.dto.OwnerProfileResponse;
import org.staymatch.pgdiscovery.model.OwnerProfile;

@Component
public class OwnerProfileMapper {

    public OwnerProfile toEntity(OwnerProfileRequest request) {
        OwnerProfile owner = new OwnerProfile();
        owner.setFullName(request.getFullName());
        owner.setEmail(request.getEmail().toLowerCase());
        owner.setPhoneNumber(request.getPhoneNumber());
        return owner;
    }

    public OwnerProfileResponse toResponse(OwnerProfile owner) {
        OwnerProfileResponse response = new OwnerProfileResponse();
        response.setId(owner.getId());
        response.setFullName(owner.getFullName());
        response.setEmail(owner.getEmail());
        response.setPhoneNumber(owner.getPhoneNumber());
        return response;
    }
}

