package com.buffetrestaurant.domain;

import com.buffetrestaurant.domain.enums.OrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer_orders")
public class CustomerOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dining_session_id", nullable = false)
    private Long sessionId;

    @Column(name = "table_number", nullable = false, length = 20)
    private String tableNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    protected CustomerOrder() {}

    public CustomerOrder(Long sessionId, String tableNumber) {
        this.sessionId = sessionId;
        this.tableNumber = tableNumber;
        this.status = OrderStatus.RECEIVED;
        this.createdAt = OffsetDateTime.now();
    }

    public void addItem(Long menuItemId, String itemName, int quantity) {
        items.add(new OrderItem(this, menuItemId, itemName, quantity));
    }

    public Long getId() { return id; }
    public Long getSessionId() { return sessionId; }
    public String getTableNumber() { return tableNumber; }
    public OrderStatus getStatus() { return status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public List<OrderItem> getItems() { return items; }
}
