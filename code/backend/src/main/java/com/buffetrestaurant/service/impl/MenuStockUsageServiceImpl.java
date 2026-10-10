package com.buffetrestaurant.service.impl;

import com.buffetrestaurant.domain.*;
import com.buffetrestaurant.dto.request.MenuItemRequest;
import com.buffetrestaurant.dto.response.MenuStockUsageResponse;
import com.buffetrestaurant.exception.*;
import com.buffetrestaurant.repository.*;
import com.buffetrestaurant.service.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MenuStockUsageServiceImpl implements MenuStockUsageService {
    private final StockItemRepository stocks;
    private final MenuItemRepository menus;
    private final MenuAdminAccessProvider access;
    public MenuStockUsageServiceImpl(StockItemRepository stocks, MenuItemRepository menus, MenuAdminAccessProvider access) {
        this.stocks = stocks; this.menus = menus; this.access = access;
    }
    @Override
    public void configure(MenuItem item, MenuItemRequest request) {
        // Older clients can edit presentation fields without erasing an existing recipe.
        if (request.automaticStockDeduction() == null && request.stockUsage() == null) return;
        boolean enabled = Boolean.TRUE.equals(request.automaticStockDeduction());
        var requested = request.stockUsage() == null ? List.<com.buffetrestaurant.dto.request.MenuStockUsageRequest>of() : request.stockUsage();
        if (!enabled) {
            if (!requested.isEmpty()) throw new BusinessRuleException("Disabled stock deduction must have no recipe");
            item.replaceStockUsage(false, List.of()); return;
        }
        if (requested.isEmpty()) throw new BusinessRuleException("Automatic stock deduction requires at least one stock item");
        Map<Long, com.buffetrestaurant.dto.request.MenuStockUsageRequest> sorted = new TreeMap<>();
        for (var entry : requested) {
            if (entry == null || entry.stockItemId() == null || entry.stockItemId() <= 0 || entry.quantityPerServing() == null
                    || entry.quantityPerServing().signum() <= 0 || entry.quantityPerServing().scale() > 3
                    || entry.quantityPerServing().compareTo(new java.math.BigDecimal("999999999.999")) > 0)
                throw new BusinessRuleException("Recipe quantity must be positive with at most three decimal places");
            if (sorted.putIfAbsent(entry.stockItemId(), entry) != null) throw new BusinessRuleException("Duplicate stock item in recipe");
        }
        // A manager may edit menu presentation while preserving a historical recipe whose stock is now inactive.
        // Revalidate stock availability only when the recipe itself changes.
        var existing = item.getStockUsage();
        boolean unchangedRecipe = item.isAutomaticStockDeduction()
                && existing.size() == sorted.size()
                && existing.stream().allMatch(usage -> {
                    var entry = sorted.get(usage.getStockItemId());
                    return entry != null && entry.quantityPerServing().compareTo(usage.getQuantityPerServing()) == 0;
                });
        if (unchangedRecipe) return;
        List<MenuStockUsage> recipe = new ArrayList<>();
        for (var entry : sorted.values()) {
            StockItem stock = stocks.findByIdForUpdate(entry.stockItemId()).orElseThrow(() -> new ResourceNotFoundException("Stock item not found: " + entry.stockItemId()));
            if (!stock.isActive() || stock.isArchived()) throw new ResourceConflictException("วัตถุดิบใช้งานไม่ได้: " + stock.getName());
            recipe.add(new MenuStockUsage(stock.getId(), entry.quantityPerServing(), stock.getUnit()));
        }
        item.replaceStockUsage(true, recipe);
    }
    @Override
    @Transactional(readOnly = true)
    public MenuStockUsageResponse read(Long id) {
        access.requireMenuWriteAccess();
        MenuItem menu = menus.findById(id).orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + id));
        return new MenuStockUsageResponse(menu.isAutomaticStockDeduction(), menu.getStockUsage().stream().map(entry -> {
            StockItem stock = stocks.findById(entry.getStockItemId()).orElseThrow(() -> new ResourceNotFoundException("Stock item not found"));
            return new MenuStockUsageResponse.Entry(stock.getId(), stock.getName(), entry.getUnit(), entry.getQuantityPerServing(), stock.isActive() && !stock.isArchived());
        }).toList());
    }
    @Override
    public void snapshot(MenuItem item, OrderItem orderedItem) {
        orderedItem.snapshotStockUsage(item.isAutomaticStockDeduction() ? item.getStockUsage() : List.of());
    }
}
