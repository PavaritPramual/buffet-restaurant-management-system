package com.buffetrestaurant.service;

import com.buffetrestaurant.domain.MenuItem;
import com.buffetrestaurant.domain.OrderItem;
import com.buffetrestaurant.dto.request.MenuItemRequest;
import com.buffetrestaurant.dto.response.MenuStockUsageResponse;

/** Called inside catalog/order transactions while the menu row is locked. */
public interface MenuStockUsageService {
    void configure(MenuItem item, MenuItemRequest request);
    MenuStockUsageResponse read(Long menuItemId);
    void snapshot(MenuItem item, OrderItem orderedItem);
}
