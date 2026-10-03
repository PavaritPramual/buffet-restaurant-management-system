package com.buffetrestaurant.service;

import com.buffetrestaurant.domain.enums.StockTransactionType;
import com.buffetrestaurant.repository.StockItemRepository;
import com.buffetrestaurant.repository.StockTransactionRepository;
import com.buffetrestaurant.repository.UserAccountRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class StockAdjustmentProcessor extends StockTransactionTemplate {
    public StockAdjustmentProcessor(StockItemRepository items, StockTransactionRepository transactions,
            UserAccountRepository users) {
        super(items, transactions, users);
    }

    @Override
    protected BigDecimal calculateDelta(BigDecimal quantity) { return quantity; }

    @Override
    protected StockTransactionType transactionType() { return StockTransactionType.ADJUSTMENT; }
}