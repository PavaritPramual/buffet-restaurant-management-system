package com.buffetrestaurant.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record StockInRequest(
        @DecimalMin(value = "0.001") BigDecimal quantity,
        @NotBlank @Size(max = 255) String reason
) {
}