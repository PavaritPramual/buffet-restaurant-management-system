package com.buffetrestaurant.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;
import java.time.OffsetDateTime;

@Entity
@Table(name = "menu_items")
public class MenuItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private MenuCategory category;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean available;

    @Column(name = "archived_at")
    private OffsetDateTime archivedAt;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "package_menu_items", joinColumns = @JoinColumn(name = "menu_item_id"))
    @Column(name = "package_id", nullable = false)
    private Set<Long> packageIds = new LinkedHashSet<>();

    @Column(name = "automatic_stock_deduction", nullable = false)
    private boolean automaticStockDeduction;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "menu_stock_usage", joinColumns = @JoinColumn(name = "menu_item_id"))
    private java.util.List<MenuStockUsage> stockUsage = new java.util.ArrayList<>();

    public boolean isAutomaticStockDeduction() { return automaticStockDeduction; }
    public java.util.List<MenuStockUsage> getStockUsage() { return java.util.List.copyOf(stockUsage); }
    public void replaceStockUsage(boolean enabled, java.util.List<MenuStockUsage> usage) {
        automaticStockDeduction = enabled;
        stockUsage.clear(); stockUsage.addAll(usage);
    }

    protected MenuItem() {}

    public MenuItem(MenuCategory category, String name, String description, boolean available,
                    String imageUrl, Set<Long> packageIds) {
        this.category = category;
        this.name = name;
        this.description = description;
        this.available = available;
        this.imageUrl = imageUrl;
        this.packageIds = new LinkedHashSet<>(packageIds);
    }

    public MenuItem(MenuCategory category, String name, boolean available, String imageUrl, Set<Long> packageIds) {
        this(category, name, null, available, imageUrl, packageIds);
    }

    public Long getId() { return id; }
    public MenuCategory getCategory() { return category; }
    public void setCategory(MenuCategory category) { this.category = category; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public OffsetDateTime getArchivedAt() { return archivedAt; }
    public boolean isArchived() { return archivedAt != null; }
    public void archive() {
        if (!isArchived()) { archivedAt = OffsetDateTime.now(java.time.ZoneOffset.UTC); available = false; }
    }
    public void restore() {
        if (isArchived()) { archivedAt = null; available = false; }
    }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Set<Long> getPackageIds() { return packageIds; }
    public void setPackageIds(Set<Long> packageIds) { this.packageIds = new LinkedHashSet<>(packageIds); }
}
