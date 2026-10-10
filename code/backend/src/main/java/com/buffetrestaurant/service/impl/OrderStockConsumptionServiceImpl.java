package com.buffetrestaurant.service.impl;

import com.buffetrestaurant.domain.*;
import com.buffetrestaurant.domain.enums.UserRole;
import com.buffetrestaurant.exception.ResourceConflictException;
import com.buffetrestaurant.repository.StockItemRepository;
import com.buffetrestaurant.service.*;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderStockConsumptionServiceImpl implements OrderStockConsumptionService {
    private final StockItemRepository stocks;
    private final StockConsumptionTransactionProcessor processor;
    private final UserContextProvider users;
    public OrderStockConsumptionServiceImpl(StockItemRepository stocks, StockConsumptionTransactionProcessor processor, UserContextProvider users) {
        this.stocks = stocks; this.processor = processor; this.users = users;
    }
    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    public void consume(CustomerOrder order) {
        Map<Long, BigDecimal> totals = new TreeMap<>();
        Map<Long, String> units = new HashMap<>();
        for (OrderItem item : order.getItems()) for (MenuStockUsage usage : item.getStockUsage()) {
            if (units.putIfAbsent(usage.getStockItemId(), usage.getUnit()) != null && !units.get(usage.getStockItemId()).equals(usage.getUnit()))
                throw new ResourceConflictException("หน่วยวัตถุดิบในออเดอร์ไม่ตรงกัน");
            totals.merge(usage.getStockItemId(), usage.getQuantityPerServing().multiply(BigDecimal.valueOf(item.getQuantity())), BigDecimal::add);
        }
        if (totals.isEmpty()) return; // Pre-upgrade/untracked orders have no consumption.
        var actor = users.requireCurrentRequestRole(UserRole.KITCHEN_STAFF);
        Map<Long, StockItem> locked = new LinkedHashMap<>();
        List<String> problems = new ArrayList<>();
        for (var entry : totals.entrySet()) {
            StockItem stock = stocks.findByIdForUpdate(entry.getKey()).orElseThrow(() -> new ResourceConflictException("ไม่พบวัตถุดิบ #" + entry.getKey()));
            locked.put(entry.getKey(), stock);
            if (!stock.isActive() || stock.isArchived()) problems.add(stock.getName() + " ใช้งานไม่ได้");
            else if (!stock.getUnit().equals(units.get(entry.getKey()))) problems.add(stock.getName() + " หน่วยไม่ตรงกับสูตร");
            else if (stock.getQuantity().compareTo(entry.getValue()) < 0)
                problems.add(stock.getName() + " ต้องใช้ " + entry.getValue().toPlainString() + " " + stock.getUnit() + " มี " + stock.getQuantity().toPlainString());
        }
        if (!problems.isEmpty()) throw new ResourceConflictException("เริ่มทำไม่ได้: " + String.join("; ", problems));
        totals.forEach((id, quantity) -> processor.consume(locked.get(id), quantity, order.getId(), actor));
    }
}
