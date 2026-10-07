package com.buffetrestaurant.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record StockItemRequest(
        @NotBlank @Size(max = 40)
        @Schema(description = "Unique stock item code", example = "ING-001")
        String sku,
        @NotBlank @Size(max = 120)
        @Schema(description = "Stock item name", example = "Rice")
        String name,
        @NotBlank @Size(max = 24)
        @Schema(description = "Measurement unit", example = "kg")
        String unit,
        @NotNull @DecimalMin("0") @Digits(integer = 9, fraction = 3)
        @Schema(description = "Low-stock notification threshold; JSON number with up to three decimal places",
                type = "number", format = "double", implementation = Double.class, example = "2.500")
        BigDecimal lowStockThreshold
) {
}
