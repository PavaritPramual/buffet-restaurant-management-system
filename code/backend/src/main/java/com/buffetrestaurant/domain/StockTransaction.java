package com.buffetrestaurant.domain;

import com.buffetrestaurant.domain.enums.StockTransactionType;
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
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "stock_transactions")
public class StockTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_item_id", nullable = false)
    private StockItem stockItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private StockTransactionType transactionType;

    @Column(name = "quantity_delta", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantityDelta;

    @Column(name = "balance_after", nullable = false, precision = 12, scale = 3)
    private BigDecimal balanceAfter;

    @Column(nullable = false, length = 255)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id")
    private UserAccount actor;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "order_id")
    private Long orderId;
    public Long getOrderId() { return orderId; }

    protected StockTransaction() {
    }

    public StockTransaction(StockItem stockItem, StockTransactionType transactionType,
            BigDecimal quantityDelta, BigDecimal balanceAfter, String reason, UserAccount actor) {
        this(stockItem, transactionType, quantityDelta, balanceAfter, reason, actor, null);
    }

    public StockTransaction(StockItem stockItem, StockTransactionType transactionType,
            BigDecimal quantityDelta, BigDecimal balanceAfter, String reason, UserAccount actor, Long orderId) {
        this.orderId = orderId;
        this.stockItem = stockItem;
        this.transactionType = transactionType;
        this.quantityDelta = quantityDelta;
        this.balanceAfter = balanceAfter;
        this.reason = reason;
        this.actor = actor;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public StockItem getStockItem() { return stockItem; }
    public StockTransactionType getTransactionType() { return transactionType; }
    public BigDecimal getQuantityDelta() { return quantityDelta; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public String getReason() { return reason; }
    public UserAccount getActor() { return actor; }
    public Instant getCreatedAt() { return createdAt; }
}
