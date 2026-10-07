package com.buffetrestaurant.dto.response;

import com.buffetrestaurant.domain.StockTransaction;
import com.buffetrestaurant.domain.enums.StockTransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;

public record StockTransactionResponse(
        @Schema(example = "45") Long id,
        @Schema(example = "17") Long stockItemId,
        @Schema(example = "Rice") String itemName,
        @Schema(example = "IN") StockTransactionType transactionType,
        @Schema(description = "Signed quantity change as a JSON number", type = "number", format = "double",
                implementation = Double.class, example = "2.500")
        BigDecimal quantityDelta,
        @Schema(description = "Balance after transaction as a JSON number", type = "number", format = "double",
                implementation = Double.class, example = "12.500")
        BigDecimal balanceAfter,
        @Schema(example = "Delivery received") String reason,
        @Schema(description = "Username of the authenticated actor, or system when no actor is recorded", example = "staff01")
        String actorUsername,
        @Schema(description = "ISO-8601 UTC timestamp", example = "2026-10-07T08:09:10Z")
        Instant createdAt
) {
    public static StockTransactionResponse from(StockTransaction transaction) {
        return new StockTransactionResponse(transaction.getId(), transaction.getStockItem().getId(),
                transaction.getStockItem().getName(), transaction.getTransactionType(),
                transaction.getQuantityDelta(), transaction.getBalanceAfter(), transaction.getReason(),
                transaction.getActor() == null ? "system" : transaction.getActor().getUsername(),
                transaction.getCreatedAt());
    }
}