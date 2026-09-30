package com.buffetrestaurant.service;

import com.buffetrestaurant.domain.StockItem;
import com.buffetrestaurant.domain.StockTransaction;
import com.buffetrestaurant.domain.UserAccount;
import com.buffetrestaurant.domain.enums.StockTransactionType;
import com.buffetrestaurant.dto.response.UserContext;
import com.buffetrestaurant.repository.StockItemRepository;
import com.buffetrestaurant.repository.StockTransactionRepository;
import com.buffetrestaurant.repository.UserAccountRepository;
import java.math.BigDecimal;

public abstract class StockTransactionTemplate {
    private final StockItemRepository items;
    private final StockTransactionRepository transactions;
    private final UserAccountRepository users;

    protected StockTransactionTemplate(StockItemRepository items, StockTransactionRepository transactions,
            UserAccountRepository users) {
        this.items = items;
        this.transactions = transactions;
        this.users = users;
    }

    public final StockTransaction process(StockItem item, BigDecimal requestedQuantity, String reason,
            UserContext actor) {
        validate(requestedQuantity, reason, actor);
        BigDecimal delta = calculateDelta(requestedQuantity);
        BigDecimal nextBalance = item.getQuantity().add(delta);
        if (nextBalance.signum() < 0) {
            throw new IllegalStateException("Adjustment cannot reduce stock below zero");
        }
        item.applyDelta(delta);
        items.save(item);
        UserAccount account = users.getReferenceById(actor.userId());
        StockTransaction transaction = transactions.save(new StockTransaction(item, transactionType(), delta,
                nextBalance, reason.trim(), account));
        return transaction;
    }

    private void validate(BigDecimal quantity, String reason, UserContext actor) {
        if (quantity == null || quantity.signum() == 0) {
            throw new IllegalStateException("Quantity must be non-zero");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalStateException("Reason is required");
        }
        if (reason.length() > 255) {
            throw new IllegalStateException("Reason must be 255 characters or fewer");
        }
        if (actor == null || actor.userId() == null) {
            throw new IllegalStateException("A signed-in user is required");
        }
        validateQuantity(quantity);
    }

    protected abstract void validateQuantity(BigDecimal quantity);
    protected abstract BigDecimal calculateDelta(BigDecimal quantity);
    protected abstract StockTransactionType transactionType();

}