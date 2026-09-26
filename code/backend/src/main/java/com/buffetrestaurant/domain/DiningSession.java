package com.buffetrestaurant.domain;

import com.buffetrestaurant.domain.enums.DiningSessionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "dining_sessions")
public class DiningSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "table_id", nullable = false)
    private RestaurantTable restaurantTable;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "package_id", nullable = false)
    private BuffetPackage buffetPackage;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "soup_id", nullable = false)
    private Soup soup;

    @Column(name = "adult_count", nullable = false)
    private Integer adultCount;

    @Column(name = "child_count", nullable = false)
    private Integer childCount;

    @Column(name = "session_token", nullable = false, unique = true, length = 100)
    private String sessionToken;

    @Column(name = "start_time", nullable = false, updatable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DiningSessionStatus status = DiningSessionStatus.ACTIVE;

    protected DiningSession() {
    }

    public DiningSession(
            RestaurantTable restaurantTable,
            BuffetPackage buffetPackage,
            Soup soup,
            Integer adultCount,
            Integer childCount,
            String sessionToken,
            LocalDateTime startTime
    ) {
        this.restaurantTable = restaurantTable;
        this.buffetPackage = buffetPackage;
        this.soup = soup;
        this.adultCount = adultCount;
        this.childCount = childCount;
        this.sessionToken = sessionToken;
        this.startTime = startTime;
        this.status = DiningSessionStatus.ACTIVE;
    }

    public Long getId() { return id; }
    public RestaurantTable getRestaurantTable() { return restaurantTable; }
    public BuffetPackage getBuffetPackage() { return buffetPackage; }
    public Soup getSoup() { return soup; }
    public Integer getAdultCount() { return adultCount; }
    public Integer getChildCount() { return childCount; }
    public String getSessionToken() { return sessionToken; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public DiningSessionStatus getStatus() { return status; }

    public void complete(LocalDateTime completedAt) {
        this.status = DiningSessionStatus.COMPLETED;
        this.endTime = completedAt;
    }
}
