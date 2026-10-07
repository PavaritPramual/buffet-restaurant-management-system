package com.buffetrestaurant.service;

import com.buffetrestaurant.domain.StockItem;
import com.buffetrestaurant.domain.StockTransaction;
import com.buffetrestaurant.dto.response.UserContext;
import java.math.BigDecimal;

/** Internal stock workflow, called after service validation and under its row lock/transaction. */
public interface StockTransactionProcessor {
    StockTransaction process(StockItem item, BigDecimal requestedQuantity, String reason, UserContext actor);
}
