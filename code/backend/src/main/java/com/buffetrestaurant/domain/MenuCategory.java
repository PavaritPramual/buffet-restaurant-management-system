package com.buffetrestaurant.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "menu_categories")
public class MenuCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "archived_at")
    private OffsetDateTime archivedAt;

    protected MenuCategory() {}

    public MenuCategory(String name) { this.name = name; }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public OffsetDateTime getArchivedAt() { return archivedAt; }
    public boolean isArchived() { return archivedAt != null; }
    public void archive() { if (!isArchived()) archivedAt = OffsetDateTime.now(java.time.ZoneOffset.UTC); }
    public void restore() { archivedAt = null; }
}
