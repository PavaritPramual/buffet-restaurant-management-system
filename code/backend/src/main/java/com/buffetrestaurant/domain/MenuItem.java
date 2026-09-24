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

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "package_menu_items", joinColumns = @JoinColumn(name = "menu_item_id"))
    @Column(name = "package_id", nullable = false)
    private Set<Long> packageIds = new LinkedHashSet<>();

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
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Set<Long> getPackageIds() { return packageIds; }
    public void setPackageIds(Set<Long> packageIds) { this.packageIds = new LinkedHashSet<>(packageIds); }
}
