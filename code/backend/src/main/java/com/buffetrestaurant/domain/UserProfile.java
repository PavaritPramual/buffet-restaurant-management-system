package com.buffetrestaurant.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_profiles")
public class UserProfile {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @MapsId
    @OneToOne
    @JoinColumn(name = "user_id")
    private UserAccount user;

    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @Column(length = 254)
    private String email;

    protected UserProfile() {
    }

    public UserProfile(UserAccount user, String displayName, String email) {
        this.user = user;
        this.displayName = displayName;
        this.email = email;
    }

    public Long getUserId() { return userId; }
    public String getDisplayName() { return displayName; }
    public String getEmail() { return email; }
}