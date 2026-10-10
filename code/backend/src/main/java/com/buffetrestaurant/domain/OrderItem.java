package com.buffetrestaurant.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private CustomerOrder order;

    @Column(name = "menu_item_id", nullable = false)
    private Long menuItemId;

    @Column(name = "item_name", nullable = false, length = 100)
    private String itemName;

    @Column(nullable = false)
    private int quantity;

    @Column(length = 255)
    private String note;

    @jakarta.persistence.ElementCollection(fetch = FetchType.LAZY)
    @jakarta.persistence.CollectionTable(name = "order_item_stock_usage", joinColumns = @JoinColumn(name = "order_item_id"))
    private java.util.List<MenuStockUsage> stockUsage = new java.util.ArrayList<>();

    public void snapshotStockUsage(java.util.List<MenuStockUsage> usage) {
        stockUsage.clear();
        usage.forEach(entry -> stockUsage.add(new MenuStockUsage(entry.getStockItemId(),
                entry.getQuantityPerServing(), entry.getUnit())));
    }
    public java.util.List<MenuStockUsage> getStockUsage() { return java.util.List.copyOf(stockUsage); }

    protected OrderItem() {}

    OrderItem(CustomerOrder order, Long menuItemId, String itemName, int quantity) {
        this.order = order;
        this.menuItemId = menuItemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.note = null;
    }

    public Long getMenuItemId() { return menuItemId; }
    public String getItemName() { return itemName; }
    public int getQuantity() { return quantity; }
    public String getNote() { return note; }
}
