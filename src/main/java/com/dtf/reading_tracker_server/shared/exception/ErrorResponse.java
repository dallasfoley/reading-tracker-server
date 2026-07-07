package com.dtf.reading_tracker_server.shared.exception;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        String traceId,
        List<FieldError> fieldErrors
) {
    public ErrorResponse(String message) {
        this(Instant.now(), 0, null, message, null, null, List.of());
    }

    public record FieldError(
            String field,
            String message
    ) {
    }
}
