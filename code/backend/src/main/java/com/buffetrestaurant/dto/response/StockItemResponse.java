package com.buffetrestaurant.dto.response;

import com.buffetrestaurant.domain.StockItem;
import java.math.BigDecimal;
import java.time.Instant;

public record StockItemResponse(Long id, String sku, String name, String unit, BigDecimal quantity,
        BigDecimal lowStockThreshold, BigDecimal openingTargetStock, BigDecimal shortfall, boolean active,
        Instant updatedAt) {
    public static StockItemResponse from(StockItem item) {
        return new StockItemResponse(item.getId(), item.getSku(), item.getName(), item.getUnit(),
                item.getQuantity(), item.getLowStockThreshold(), item.getOpeningTargetStock(),
                item.getShortfall(), item.isActive(), item.getUpdatedAt());
    }
}
