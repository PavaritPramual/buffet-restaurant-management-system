package com.buffetrestaurant.controller;

import com.buffetrestaurant.common.ApiPaths;
import com.buffetrestaurant.dto.request.PlaceOrderRequest;
import com.buffetrestaurant.dto.response.MenuItemResponse;
import com.buffetrestaurant.dto.response.OrderResponse;
import com.buffetrestaurant.service.CustomerOrderingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.API_V1)
@Tag(name = "Customer Ordering")
public class CustomerOrderingController {
    private final CustomerOrderingService service;
    public CustomerOrderingController(CustomerOrderingService service) { this.service = service; }

    @GetMapping("/dining-sessions/{sessionId}/menu")
    @Operation(summary = "Get available menu items allowed by the active session package",
            description = "Requires X-Session-Token matching the active dining session in the path")
    public List<MenuItemResponse> menu(@PathVariable Long sessionId,
                                       @Parameter(description = "Bearer token from the QR code")
                                       @RequestHeader(name = "X-Session-Token", required = false) String token) {
        return service.getMenu(sessionId, token);
    }

    @PostMapping("/dining-sessions/{sessionId}/orders")
    @Operation(summary = "Place a customer order with initial RECEIVED status",
            description = "Requires X-Session-Token matching the active dining session in the path")
    public ResponseEntity<OrderResponse> place(@PathVariable Long sessionId,
                                               @Parameter(description = "Bearer token from the QR code")
                                               @RequestHeader(name = "X-Session-Token", required = false) String token,
                                               @Valid @RequestBody PlaceOrderRequest request) {
        OrderResponse created = service.placeOrder(sessionId, token, request);
        return ResponseEntity.created(URI.create(ApiPaths.API_V1 + "/dining-sessions/" + sessionId
                + "/orders/" + created.orderId())).body(created);
    }

    @GetMapping("/dining-sessions/{sessionId}/orders")
    @Operation(summary = "Get orders for an active session; requires the matching X-Session-Token")
    public List<OrderResponse> orders(@PathVariable Long sessionId,
                                     @Parameter(description = "Bearer token from the QR code")
                                     @RequestHeader(name = "X-Session-Token", required = false) String token) {
        return service.getOrders(sessionId, token);
    }

    @GetMapping("/dining-sessions/{sessionId}/orders/{orderId}")
    @Operation(summary = "Get one order for an active session; requires the matching X-Session-Token")
    public OrderResponse order(@PathVariable Long sessionId, @PathVariable Long orderId,
                               @Parameter(description = "Bearer token from the QR code")
                               @RequestHeader(name = "X-Session-Token", required = false) String token) {
        return service.getOrder(sessionId, token, orderId);
    }
}
