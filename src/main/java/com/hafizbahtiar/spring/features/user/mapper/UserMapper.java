package com.hafizbahtiar.spring.features.user.mapper;

import com.hafizbahtiar.spring.features.user.dto.UpdateProfileRequest;
import com.hafizbahtiar.spring.features.user.dto.UserProfileResponse;
import com.hafizbahtiar.spring.features.user.dto.UserRegistrationRequest;
import com.hafizbahtiar.spring.features.user.dto.UserResponse;
import com.hafizbahtiar.spring.features.user.dto.UserUpdateRequest;
import com.hafizbahtiar.spring.features.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "emailVerified", constant = "false")
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "role", constant = "USER")
    User toEntity(UserRegistrationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    User updateEntityFromRequest(UserUpdateRequest request, @MappingTarget User user);

    /**
     * Update user entity from UpdateProfileRequest (only profile fields).
     * Ignores fields that should not be updated via profile update.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateProfileFromRequest(UpdateProfileRequest request, @MappingTarget User user);

    UserResponse toResponse(User user);

    UserProfileResponse toProfileResponse(User user);

    List<UserResponse> toResponseList(List<User> users);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "active", source = "active")
    @Mapping(target = "emailVerified", source = "emailVerified")
    UserResponse.Summary toSummary(User user);

    List<UserResponse.Summary> toSummaryList(List<User> users);
}

