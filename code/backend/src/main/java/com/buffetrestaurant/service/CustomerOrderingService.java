package com.buffetrestaurant.service;

import com.buffetrestaurant.dto.request.PlaceOrderRequest;
import com.buffetrestaurant.dto.response.MenuItemResponse;
import com.buffetrestaurant.dto.response.OrderResponse;
import java.util.List;

public interface CustomerOrderingService {
    List<MenuItemResponse> getMenu(Long sessionId);
    OrderResponse placeOrder(Long sessionId, PlaceOrderRequest request);
    List<OrderResponse> getOrders(Long sessionId);
    OrderResponse getOrder(Long orderId);
}
