package com.buffetrestaurant.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buffetrestaurant.domain.StockItem;
import com.buffetrestaurant.domain.StockTransaction;
import com.buffetrestaurant.domain.UserAccount;
import com.buffetrestaurant.domain.enums.StockTransactionType;
import com.buffetrestaurant.domain.enums.UserRole;
import com.buffetrestaurant.dto.response.UserContext;
import com.buffetrestaurant.repository.StockItemRepository;
import com.buffetrestaurant.repository.StockTransactionRepository;
import com.buffetrestaurant.repository.UserAccountRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class StockTransactionTemplateTest {
    private final StockItemRepository items = mock(StockItemRepository.class);
    private final StockTransactionRepository transactions = mock(StockTransactionRepository.class);
    private final UserAccountRepository users = mock(UserAccountRepository.class);
    private final StockInProcessor stockIn = new StockInProcessor(items, transactions, users);
    private final StockAdjustmentProcessor adjustment = new StockAdjustmentProcessor(items, transactions, users);
    private final StockItem item = new StockItem("ING-1", "Rice", "kg", new BigDecimal("5.000"), BigDecimal.ZERO);
    private final UserContext actor = new UserContext(4L, "staff", "Service Staff", UserRole.SERVICE_STAFF);

    @Test
    void stockInUpdatesBalanceAndCreatesAuditedTransaction() {
        UserAccount account = new UserAccount("staff", "hash", UserRole.SERVICE_STAFF);
        when(users.getReferenceById(4L)).thenReturn(account);
        when(transactions.save(any(StockTransaction.class))).thenAnswer(call -> call.getArgument(0));

        StockTransaction transaction = stockIn.process(item, new BigDecimal("2.500"), "Received delivery", actor);

        assertEquals(new BigDecimal("7.500"), item.getQuantity());
        assertEquals(StockTransactionType.IN, transaction.getTransactionType());
        assertEquals(new BigDecimal("7.500"), transaction.getBalanceAfter());
        assertEquals("staff", transaction.getActor().getUsername());
        verify(items).save(item);
        verify(transactions).save(any(StockTransaction.class));
    }

    @Test
    void adjustmentAllowsNegativeDeltaButRejectsNegativeBalance() {
        when(users.getReferenceById(4L)).thenReturn(new UserAccount("staff", "hash", UserRole.SERVICE_STAFF));
        when(transactions.save(any(StockTransaction.class))).thenAnswer(call -> call.getArgument(0));

        StockTransaction transaction = adjustment.process(item, new BigDecimal("-2.000"), "Spoilage", actor);
        assertEquals(new BigDecimal("3.000"), item.getQuantity());
        assertEquals(StockTransactionType.ADJUSTMENT, transaction.getTransactionType());

        assertThrows(IllegalStateException.class, () -> adjustment.process(item,
                new BigDecimal("-4.000"), "Count correction", actor));
    }

    @Test
    void rejectsZeroQuantityAndBlankReason() {
        assertThrows(IllegalStateException.class, () -> adjustment.process(item, BigDecimal.ZERO, "Count", actor));
        assertThrows(IllegalStateException.class, () -> stockIn.process(item,
                BigDecimal.ONE, "  ", actor));
        assertThrows(IllegalStateException.class, () -> stockIn.process(item,
                new BigDecimal("-1"), "Invalid receipt", actor));
    }
}