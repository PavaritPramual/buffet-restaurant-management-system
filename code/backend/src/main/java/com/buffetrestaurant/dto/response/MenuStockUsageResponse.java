package com.buffetrestaurant.dto.response;

import java.math.BigDecimal;
import java.util.List;

/** Manager-only recipe data; not part of the shared customer menu response. */
public record MenuStockUsageResponse(boolean automaticStockDeduction, List<Entry> stockUsage) {
    public record Entry(Long stockItemId, String stockItemName, String unit,
            BigDecimal quantityPerServing, boolean active) {}
}
