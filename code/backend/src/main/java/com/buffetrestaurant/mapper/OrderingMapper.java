package com.buffetrestaurant.mapper;

import com.buffetrestaurant.domain.CustomerOrder;
import com.buffetrestaurant.domain.MenuCategory;
import com.buffetrestaurant.domain.MenuItem;
import com.buffetrestaurant.dto.response.MenuCategoryResponse;
import com.buffetrestaurant.dto.response.MenuItemResponse;
import com.buffetrestaurant.dto.response.OrderResponse;
import java.util.LinkedHashSet;
import org.springframework.stereotype.Component;

@Component
public class OrderingMapper {
    public MenuCategoryResponse toResponse(MenuCategory category) {
        return new MenuCategoryResponse(category.getId(), category.getName());
    }

    public MenuItemResponse toResponse(MenuItem item) {
        return new MenuItemResponse(item.getId(), item.getCategory().getId(), item.getCategory().getName(),
                item.getName(), item.getDescription(), item.isAvailable(),
                new LinkedHashSet<>(item.getPackageIds()), item.getImageUrl());
    }

    public OrderResponse toResponse(CustomerOrder order) {
        return new OrderResponse(order.getId(), order.getSessionId(), order.getTableNumber(),
                order.getItems().stream()
                        .map(item -> new OrderResponse.Item(item.getMenuItemId(), item.getItemName(), item.getQuantity()))
                        .toList(),
                order.getStatus(), order.getCreatedAt());
    }
}
