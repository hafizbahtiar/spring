package com.hafizbahtiar.spring.features.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Request DTO for creating or updating a certification.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificationRequest {

    @NotBlank(message = "Certification name is required")
    @Size(max = 200, message = "Certification name must not exceed 200 characters")
    private String name;

    @NotBlank(message = "Issuer is required")
    @Size(max = 100, message = "Issuer must not exceed 100 characters")
    private String issuer;

    @Size(max = 100, message = "Credential ID must not exceed 100 characters")
    private String credentialId;

    @Size(max = 500, message = "Credential URL must not exceed 500 characters")
    private String credentialUrl;

    @NotNull(message = "Issue date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate issueDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expiryDate;

    private Boolean isVerified = false;

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    private Integer displayOrder;
}
