package com.hafizbahtiar.spring.features.user.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for uploading avatar.
 * Supports both file upload (MultipartFile) and URL string.
 * Only one should be provided at a time.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvatarUploadRequest {

    /**
     * Avatar URL (if uploading via URL)
     */
    @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
    private String avatarUrl;
}

