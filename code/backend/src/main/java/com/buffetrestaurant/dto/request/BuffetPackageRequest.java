package com.buffetrestaurant.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record BuffetPackageRequest(
        @NotBlank(message = "Package name is required")
        @Size(max = 100, message = "Package name must not exceed 100 characters")
        String name,
        @NotNull(message = "Package price is required")
        @DecimalMin(value = "0.01", message = "Package price must be greater than zero")
        @Digits(integer = 8, fraction = 2, message = "Package price must fit DECIMAL(10,2)")
        BigDecimal price,
        String description
) {
}