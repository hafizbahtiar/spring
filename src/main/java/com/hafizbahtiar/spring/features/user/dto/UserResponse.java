package com.hafizbahtiar.spring.features.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private UUID uuid;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phone;
    private String bio;
    private String location;
    private String website;
    private String avatarUrl;
    private String role;
    private Boolean emailVerified;
    private Boolean active;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastLoginAt;

    // Inner class for summary responses
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Long id;
        private UUID uuid;
        private String email;
        private String username;
        private String fullName;
        private String role;
        private Boolean active;
        private Boolean emailVerified;
    }
}
