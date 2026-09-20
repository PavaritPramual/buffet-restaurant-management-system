package com.buffetrestaurant.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateTableRequest(
        @NotBlank(message = "Table number is required")
        @Size(max = 20, message = "Table number must not exceed 20 characters")
        String tableNumber,

        @NotNull(message = "Capacity is required")
        @Min(value = 1, message = "Capacity must be at least 1")
        Integer capacity
) {
}
