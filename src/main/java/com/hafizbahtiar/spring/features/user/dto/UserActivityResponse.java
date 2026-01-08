package com.hafizbahtiar.spring.features.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for user activity logs.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityResponse {

    private String id;
    private Long userId;
    private String sessionId;
    private String activityType;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private ActivityDetailsResponse details;
    private Object metadata;

    /**
     * Inner class for activity details
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityDetailsResponse {
        private String endpoint;
        private String method;
        private Integer responseStatus;
        private Long responseTimeMs;
        private String userAgent;
        private String ipAddress;
        private String requestId;
        private Object additionalData;
    }
}
