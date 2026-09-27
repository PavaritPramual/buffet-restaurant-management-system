package com.buffetrestaurant.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SoupRequest(
        @NotBlank(message = "Soup name is required")
        @Size(max = 100, message = "Soup name must not exceed 100 characters")
        String name
) {
}