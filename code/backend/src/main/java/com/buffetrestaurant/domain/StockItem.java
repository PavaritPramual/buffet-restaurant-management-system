package com.buffetrestaurant.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "stock_items")
public class StockItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String sku;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 24)
    private String unit;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    @Column(name = "low_stock_threshold", nullable = false, precision = 12, scale = 3)
    private BigDecimal lowStockThreshold;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected StockItem() {
    }

    public StockItem(String sku, String name, String unit, BigDecimal quantity, BigDecimal lowStockThreshold) {
        this.sku = sku;
        this.name = name;
        this.unit = unit;
        this.quantity = quantity;
        this.lowStockThreshold = lowStockThreshold;
        this.updatedAt = Instant.now();
    }

    public void applyDelta(BigDecimal delta) {
        this.quantity = this.quantity.add(delta);
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    void touch() { this.updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public String getUnit() { return unit; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getLowStockThreshold() { return lowStockThreshold; }
    public Instant getUpdatedAt() { return updatedAt; }
}