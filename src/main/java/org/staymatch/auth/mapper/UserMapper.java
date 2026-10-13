package org.staymatch.auth.mapper;

import org.springframework.stereotype.Component;
import org.staymatch.auth.dto.UserResponse;
import org.staymatch.auth.model.AppUser;

@Component("appUserMapper")
public class UserMapper {

    public UserResponse toResponse(AppUser user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setRole(user.getRole().name());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}

