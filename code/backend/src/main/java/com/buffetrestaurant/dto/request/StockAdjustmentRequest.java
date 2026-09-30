package com.buffetrestaurant.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record StockAdjustmentRequest(
        BigDecimal quantityDelta,
        @NotBlank @Size(max = 255) String reason
) {
}