package com.buffetrestaurant.dto.response;

import com.buffetrestaurant.domain.StockTransaction;
import com.buffetrestaurant.domain.enums.StockTransactionType;
import java.math.BigDecimal;
import java.time.Instant;

public record StockTransactionResponse(Long id, Long stockItemId, String itemName,
        StockTransactionType transactionType, BigDecimal quantityDelta, BigDecimal balanceAfter,
        String reason, String actorUsername, Instant createdAt) {
    public static StockTransactionResponse from(StockTransaction transaction) {
        return new StockTransactionResponse(transaction.getId(), transaction.getStockItem().getId(),
                transaction.getStockItem().getName(), transaction.getTransactionType(),
                transaction.getQuantityDelta(), transaction.getBalanceAfter(), transaction.getReason(),
                transaction.getActor() == null ? "system" : transaction.getActor().getUsername(),
                transaction.getCreatedAt());
    }
}