package com.buffetrestaurant.dto.response;

import java.time.OffsetDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard error body returned for API failures")
public record ErrorResponse(
        @Schema(description = "UTC time when the error was created", example = "2026-10-07T08:09:10Z")
        OffsetDateTime timestamp,
        @Schema(description = "HTTP status code", example = "400")
        int status,
        @Schema(description = "HTTP reason phrase", example = "Bad Request")
        String error,
        @Schema(description = "Human-readable error message", example = "sessionId: must be greater than 0")
        String message,
        @Schema(description = "Request path", example = "/api/v1/billing/preview")
        String path
) {
}
