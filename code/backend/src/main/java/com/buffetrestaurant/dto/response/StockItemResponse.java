package com.buffetrestaurant.dto.response;

import com.buffetrestaurant.domain.StockItem;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;

public record StockItemResponse(
        @Schema(example = "17") Long id,
        @Schema(example = "ING-001") String sku,
        @Schema(example = "Rice") String name,
        @Schema(example = "kg") String unit,
        @Schema(description = "Current stock quantity as a JSON number", type = "number", format = "double",
                implementation = Double.class, example = "12.500")
        BigDecimal quantity,
        @Schema(description = "Low-stock threshold as a JSON number", type = "number", format = "double",
                implementation = Double.class, example = "2.500")
        BigDecimal lowStockThreshold,
        @Schema(description = "Opening target stock as a JSON number", type = "number", format = "double",
                implementation = Double.class, example = "10.000", nullable = true)
        BigDecimal openingTargetStock,
        @Schema(description = "Shortfall against the opening target as a JSON number", type = "number",
                format = "double", implementation = Double.class, example = "2.500")
        BigDecimal shortfall,
        @Schema(description = "Whether the item can be used in stock transactions", example = "true")
        boolean active,
        @Schema(description = "ISO-8601 UTC timestamp", example = "2026-10-07T08:09:10Z")
        Instant updatedAt
) {
    public static StockItemResponse from(StockItem item) {
        return new StockItemResponse(item.getId(), item.getSku(), item.getName(), item.getUnit(),
                item.getQuantity(), item.getLowStockThreshold(), item.getOpeningTargetStock(),
                item.getShortfall(), item.isActive(), item.getUpdatedAt());
    }
}
