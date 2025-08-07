package com.example.bankcards.dto;

import lombok.*;

import java.time.Instant;
import java.util.List;

/**
 * DTO для ошибок API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String message;
    private String errorCode;
    private List<String> details;
    private Instant timestamp = Instant.now();

    public ErrorResponse(String message, String errorCode) {
        this.message = message;
        this.errorCode = errorCode;
    }
}
