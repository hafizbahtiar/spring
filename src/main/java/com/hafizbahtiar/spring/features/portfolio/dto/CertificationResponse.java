package com.hafizbahtiar.spring.features.portfolio.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for certification details.
 * Includes calculated fields for expiry status and days until expiry.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificationResponse {

    private Long id;
    private Long userId;
    private String name;
    private String issuer;
    private String credentialId;
    private String credentialUrl;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private Boolean isExpired;
    private Boolean isVerified;
    private String imageUrl;
    private String description;
    private Integer displayOrder;

    // Calculated fields
    private Long daysUntilExpiry; // null if no expiry date or already expired
    private Boolean isExpiringSoon; // true if expiring within 90 days

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Inner class for summary responses (used in nested contexts)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Summary {
        private Long id;
        private String name;
        private String issuer;
        private String imageUrl;
        private Boolean isExpired;
        private Boolean isVerified;
    }
}
