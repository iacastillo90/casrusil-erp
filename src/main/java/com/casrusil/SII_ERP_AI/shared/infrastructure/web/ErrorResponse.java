package com.casrusil.SII_ERP_AI.shared.infrastructure.web;

import java.time.Instant;

/**
 * Standard error response DTO for all API errors.
 * Provides consistent error format across the application.
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        String errorCode,
        java.util.Map<String, String> validationErrors) {

    public ErrorResponse(int status, String error, String message, String path, String errorCode) {
        this(Instant.now(), status, error, message, path, errorCode, null);
    }

    public ErrorResponse(int status, String error, String message, String path) {
        this(Instant.now(), status, error, message, path, null, null);
    }

    public ErrorResponse(int status, String error, String message, String path,
            java.util.Map<String, String> validationErrors) {
        this(Instant.now(), status, error, message, path, null, validationErrors);
    }
}
