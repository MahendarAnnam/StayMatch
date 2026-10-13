package org.staymatch.tenantmatching.mapper;

import org.springframework.stereotype.Component;
import org.staymatch.tenantmatching.dto.CreateUserRequest;
import org.staymatch.tenantmatching.dto.UserResponse;
import org.staymatch.tenantmatching.model.User;

@Component
public class UserMapper {

    public User toEntity(CreateUserRequest request) {
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail().toLowerCase());
        user.setPhoneNumber(request.getPhoneNumber());
        return user;
    }

    public UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setPhoneNumber(user.getPhoneNumber());
        return response;
    }
}

