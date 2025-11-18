package com.dukkan.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Error response DTO for consistent error format
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private String code;

    private String message;

    @Builder.Default
    private List<String> details = new ArrayList<>();

    /**
     * Create error response with code and message
     */
    public static ErrorResponse of(String code, String message) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .build();
    }

    /**
     * Create error response with code, message, and details
     */
    public static ErrorResponse of(String code, String message, List<String> details) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .details(details)
                .build();
    }
}
