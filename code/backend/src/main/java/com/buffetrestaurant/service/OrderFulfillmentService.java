package com.buffetrestaurant.service;

import com.buffetrestaurant.domain.enums.OrderStatus;
import com.buffetrestaurant.dto.response.OrderResponse;
import java.util.List;

public interface OrderFulfillmentService {

    /** Orders the kitchen still needs to act on: {@code RECEIVED} or {@code PREPARING}. */
    List<OrderResponse> getIncomingOrders();

    /** Orders the kitchen has finished and are waiting for service staff to serve: {@code READY}. */
    List<OrderResponse> getReadyOrders();

    /**
     * Advances an order to {@code requestedStatus}.
     *
     * <p>Only the single next status in {@code RECEIVED -> PREPARING -> READY -> SERVED} is ever
     * accepted; skipping ahead, moving backwards, or changing a {@code SERVED} order is rejected.
     */
    OrderResponse advanceStatus(Long orderId, OrderStatus requestedStatus);
}
