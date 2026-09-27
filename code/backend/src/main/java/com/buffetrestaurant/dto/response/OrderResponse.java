package com.buffetrestaurant.dto.response;

import com.buffetrestaurant.domain.enums.OrderStatus;
import java.time.OffsetDateTime;
import java.util.List;

public record OrderResponse(
        Long orderId,
        Long sessionId,
        String tableNumber,
        List<Item> items,
        OrderStatus status,
        OffsetDateTime createdAt
) {
    public record Item(Long menuItemId, String name, int quantity) {}
}
