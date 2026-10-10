package com.buffetrestaurant.service;

import com.buffetrestaurant.domain.*;
import com.buffetrestaurant.domain.enums.StockTransactionType;
import com.buffetrestaurant.dto.response.UserContext;
import com.buffetrestaurant.repository.*;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class StockConsumptionProcessor extends StockTransactionTemplate implements StockConsumptionTransactionProcessor {
    public StockConsumptionProcessor(StockItemRepository items, StockTransactionRepository transactions, UserAccountRepository users) {
        super(items, transactions, users);
    }
    @Override
    public StockTransaction consume(StockItem item, BigDecimal quantity, Long orderId, UserContext actor) {
        if (orderId == null || orderId <= 0) throw new IllegalArgumentException("Consumption requires an order");
        return processWithOrder(item, quantity, "ใช้วัตถุดิบสำหรับออเดอร์ #" + orderId, actor, orderId);
    }
    @Override protected BigDecimal calculateDelta(BigDecimal quantity) {
        if (quantity == null || quantity.signum() <= 0) throw new IllegalArgumentException("Consumption quantity must be positive");
        return quantity.negate();
    }
    @Override protected StockTransactionType transactionType() { return StockTransactionType.CONSUMPTION; }
}
