package com.buffetrestaurant.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import com.buffetrestaurant.dto.response.UserContext;

/** Immutable snapshots; deleting/renaming an account or catalog item cannot erase its audit. */
@Entity
@Table(name = "manager_operations")
public class ManagerOperation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 30) private String action;
    @Column(name = "resource_id", nullable = false) private Long resourceId;
    @Column(name = "resource_label", nullable = false, length = 100) private String resourceLabel;
    @Column(nullable = false, length = 500) private String reason;
    @Column(name = "actor_id", nullable = false) private Long actorId;
    @Column(name = "actor_username", nullable = false, length = 100) private String actorUsername;
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;

    protected ManagerOperation() {}
    public ManagerOperation(String action, Long resourceId, String label, String reason, UserContext actor) {
        this.action = action; this.resourceId = resourceId; this.resourceLabel = label;
        this.reason = reason; this.actorId = actor.userId(); this.actorUsername = actor.username();
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }
    public Long getId() { return id; }
    public String getAction() { return action; }
    public Long getResourceId() { return resourceId; }
    public String getResourceLabel() { return resourceLabel; }
    public String getReason() { return reason; }
    public Long getActorId() { return actorId; }
    public String getActorUsername() { return actorUsername; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
