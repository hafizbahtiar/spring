package com.hafizbahtiar.spring.common.util;

import com.hafizbahtiar.spring.common.dto.ApiResponse;
import com.hafizbahtiar.spring.common.dto.PaginatedResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Utility class for building consistent API responses.
 */
public class ResponseUtils {

    private ResponseUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Create a success response with data
     */
    public static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * Create a success response with data and message
     */
    public static <T> ResponseEntity<ApiResponse<T>> ok(T data, String message) {
        return ResponseEntity.ok(ApiResponse.success(data, message));
    }

    /**
     * Create a success response with paginated data.
     * Converts Spring Data Page<T> to stable PaginatedResponse DTO to avoid
     * serialization warnings.
     *
     * @param page Spring Data Page containing paginated results
     * @return ResponseEntity with ApiResponse wrapping PaginatedResponse
     */
    public static <T> ResponseEntity<ApiResponse<PaginatedResponse<T>>> okPage(Page<T> page) {
        PaginatedResponse<T> paginatedResponse = PaginatedResponse.fromPage(page);
        return ResponseEntity.ok(ApiResponse.success(paginatedResponse));
    }

    /**
     * Create a success response with paginated data and message.
     * Converts Spring Data Page<T> to stable PaginatedResponse DTO to avoid
     * serialization warnings.
     *
     * @param page    Spring Data Page containing paginated results
     * @param message Optional success message
     * @return ResponseEntity with ApiResponse wrapping PaginatedResponse
     */
    public static <T> ResponseEntity<ApiResponse<PaginatedResponse<T>>> okPage(Page<T> page, String message) {
        PaginatedResponse<T> paginatedResponse = PaginatedResponse.fromPage(page);
        return ResponseEntity.ok(ApiResponse.success(paginatedResponse, message));
    }

    /**
     * Create a created response (201) with data
     */
    public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(data));
    }

    /**
     * Create a created response (201) with data and message
     */
    public static <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(data, message));
    }

    /**
     * Create a no content response (204)
     */
    public static <T> ResponseEntity<ApiResponse<T>> noContent() {
        return ResponseEntity.noContent().build();
    }

    /**
     * Create a success response with message only
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(String message) {
        return ResponseEntity.ok(ApiResponse.success(message));
    }
}
