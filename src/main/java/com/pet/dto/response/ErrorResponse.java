package com.pet.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String errorCode,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors) {

    public ErrorResponse(LocalDateTime timestamp, int status, String errorCode, String error, String message, String path) {
        this(timestamp, status, errorCode, error, message, path, null);
    }
}
