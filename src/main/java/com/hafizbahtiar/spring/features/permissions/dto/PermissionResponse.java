package com.hafizbahtiar.spring.features.permissions.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hafizbahtiar.spring.features.permissions.entity.PermissionAction;
import com.hafizbahtiar.spring.features.permissions.entity.PermissionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for permission response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResponse {

    private Long id;
    private Long groupId;
    private String groupName;
    private PermissionType permissionType;
    private String resourceType;
    private String resourceIdentifier;
    private PermissionAction action;
    private Boolean granted;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}
