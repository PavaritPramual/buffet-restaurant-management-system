package com.buffetrestaurant.dto.request;

import jakarta.validation.constraints.NotNull;

public record ActiveStatusRequest(
        @NotNull(message = "Active status is required")
        Boolean active
) {
}