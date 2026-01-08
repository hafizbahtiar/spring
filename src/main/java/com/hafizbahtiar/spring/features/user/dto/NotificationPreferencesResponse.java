package com.hafizbahtiar.spring.features.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for notification preferences details.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferencesResponse {

    private Long id;
    private Long userId;
    private Boolean emailAccountActivity;
    private Boolean emailSecurityAlerts;
    private Boolean emailMarketing;
    private Boolean emailWeeklyDigest;
    private Boolean inAppSystem;
    private Boolean inAppProjects;
    private Boolean inAppMentions;
    private Boolean pushEnabled;
    private Boolean pushBrowser;
    private Boolean pushMobile;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
