package com.buffetrestaurant.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

/** Value owned by a menu; stock identity is an FK, never cascaded to StockItem. */
@Embeddable
public class MenuStockUsage {
    @Column(name = "stock_item_id", nullable = false)
    private Long stockItemId;
    @Column(name = "quantity_per_serving", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantityPerServing;
    @Column(name = "stock_unit", nullable = false, length = 24)
    private String unit;
    protected MenuStockUsage() {}
    public MenuStockUsage(Long stockItemId, BigDecimal quantityPerServing, String unit) {
        this.stockItemId = stockItemId; this.quantityPerServing = quantityPerServing; this.unit = unit;
    }
    public Long getStockItemId() { return stockItemId; }
    public BigDecimal getQuantityPerServing() { return quantityPerServing; }
    public String getUnit() { return unit; }
}
