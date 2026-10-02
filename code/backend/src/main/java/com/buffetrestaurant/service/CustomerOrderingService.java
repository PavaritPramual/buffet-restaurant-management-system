package com.buffetrestaurant.service;

import com.buffetrestaurant.dto.request.PlaceOrderRequest;
import com.buffetrestaurant.dto.response.MenuItemResponse;
import com.buffetrestaurant.dto.response.OrderResponse;
import java.util.List;

public interface CustomerOrderingService {
    List<MenuItemResponse> getMenu(Long sessionId, String sessionToken);
    OrderResponse placeOrder(Long sessionId, String sessionToken, PlaceOrderRequest request);
    List<OrderResponse> getOrders(Long sessionId, String sessionToken);
    OrderResponse getOrder(Long sessionId, String sessionToken, Long orderId);
}
