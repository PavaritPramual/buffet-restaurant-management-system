package com.buffetrestaurant.service;

import com.buffetrestaurant.domain.StockItem;
import com.buffetrestaurant.domain.StockTransaction;
import com.buffetrestaurant.dto.response.UserContext;
import java.math.BigDecimal;

public interface StockConsumptionTransactionProcessor {
    StockTransaction consume(StockItem item, BigDecimal quantity, Long orderId, UserContext actor);
}
