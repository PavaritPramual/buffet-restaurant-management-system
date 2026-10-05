package com.buffetrestaurant.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Prices, guest counts and discounts are resolved by the backend. */
public record BillingPreviewRequest(
        @NotNull(message = "Session ID is required")
        @Positive(message = "Session ID must be positive")
        @Schema(description = "Dining session to calculate", example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Long sessionId
) {
}
