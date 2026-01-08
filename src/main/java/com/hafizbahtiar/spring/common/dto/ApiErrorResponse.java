package com.hafizbahtiar.spring.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {

    private String error;
    private String message;
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private String path;
    private Map<String, String> validationErrors;

    public static ApiErrorResponse of(String error, String message, Integer status, String path) {
        return ApiErrorResponse.builder()
                .error(error)
                .message(message)
                .status(status)
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();
    }

    public static ApiErrorResponse of(String error, String message, Integer status, String path,
            Map<String, String> validationErrors) {
        return ApiErrorResponse.builder()
                .error(error)
                .message(message)
                .status(status)
                .timestamp(LocalDateTime.now())
                .path(path)
                .validationErrors(validationErrors)
                .build();
    }
}
