package com.hafizbahtiar.spring.features.portfolio.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.hafizbahtiar.spring.features.portfolio.entity.ContactSource;
import com.hafizbahtiar.spring.features.portfolio.entity.ContactStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for contact details.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactResponse {

    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String subject;
    private String message;
    private ContactStatus status;
    private String statusDisplayName;
    private ContactSource source;
    private String sourceDisplayName;
    private Object metadata;
    private LocalDateTime readAt;
    private LocalDateTime repliedAt;

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
        private String email;
        private String subject;
        private ContactStatus status;
        private String statusDisplayName;
        private ContactSource source;
        private String sourceDisplayName;
        private LocalDateTime createdAt;
    }
}
