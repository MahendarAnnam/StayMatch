package org.staymatch.pgdiscovery.mapper;

import org.springframework.stereotype.Component;
import org.staymatch.pgdiscovery.dto.SharingOptionRequest;
import org.staymatch.pgdiscovery.dto.SharingOptionResponse;
import org.staymatch.pgdiscovery.model.SharingOption;

@Component
public class SharingOptionMapper {

    public SharingOption toEntity(SharingOptionRequest request) {
        SharingOption option = new SharingOption();
        option.setSharingType(request.getSharingType());
        option.setRent(request.getRent());
        option.setAvailabilityCount(request.getAvailabilityCount());
        return option;
    }

    public SharingOptionResponse toResponse(SharingOption option) {
        SharingOptionResponse response = new SharingOptionResponse();
        response.setId(option.getId());
        response.setSharingType(option.getSharingType());
        response.setRent(option.getRent());
        response.setAvailabilityCount(option.getAvailabilityCount());
        return response;
    }
}

