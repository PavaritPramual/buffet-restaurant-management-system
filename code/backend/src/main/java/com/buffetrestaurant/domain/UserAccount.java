package com.buffetrestaurant.domain;

import com.buffetrestaurant.domain.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "app_users")
public class UserAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserRole role;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "archived_at")
    private Instant archivedAt;

    protected UserAccount() {
    }

    public UserAccount(String username, String passwordHash, UserRole role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = true;
        this.createdAt = Instant.now();
    }

    public void changeActive(boolean active) { this.active = active; }

    /** Closes the account: it can no longer log in, but the row stays for history references. */
    public void archive() {
        if (archivedAt != null) return;
        this.active = false;
        this.archivedAt = Instant.now();
    }

    /** Restoring never re-enables login; a Manager must activate the account explicitly. */
    public void restore() { this.archivedAt = null; }

    public boolean isArchived() { return archivedAt != null; }
    public Instant getArchivedAt() { return archivedAt; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public UserRole getRole() { return role; }
    public boolean isActive() { return active; }
}