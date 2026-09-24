package com.buffetrestaurant.controller;

import com.buffetrestaurant.common.ApiPaths;
import com.buffetrestaurant.dto.request.PlaceOrderRequest;
import com.buffetrestaurant.dto.response.MenuItemResponse;
import com.buffetrestaurant.dto.response.OrderResponse;
import com.buffetrestaurant.service.CustomerOrderingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.API_V1)
@Tag(name = "Customer Ordering")
public class CustomerOrderingController {
    private final CustomerOrderingService service;
    public CustomerOrderingController(CustomerOrderingService service) { this.service = service; }

    @GetMapping("/dining-sessions/{sessionId}/menu")
    @Operation(summary = "Get available menu items allowed by the active session package")
    public List<MenuItemResponse> menu(@PathVariable Long sessionId) { return service.getMenu(sessionId); }

    @PostMapping("/dining-sessions/{sessionId}/orders")
    @Operation(summary = "Place a customer order with initial RECEIVED status")
    public ResponseEntity<OrderResponse> place(@PathVariable Long sessionId,
                                               @Valid @RequestBody PlaceOrderRequest request) {
        OrderResponse created = service.placeOrder(sessionId, request);
        return ResponseEntity.created(URI.create(ApiPaths.API_V1 + "/orders/" + created.orderId())).body(created);
    }

    @GetMapping("/dining-sessions/{sessionId}/orders")
    public List<OrderResponse> orders(@PathVariable Long sessionId) { return service.getOrders(sessionId); }

    @GetMapping("/orders/{orderId}")
    public OrderResponse order(@PathVariable Long orderId) { return service.getOrder(orderId); }
}
