package com.buffetrestaurant.controller;

import com.buffetrestaurant.common.ApiPaths;
import com.buffetrestaurant.config.CustomerOriginGuard;
import com.buffetrestaurant.dto.request.PlaceOrderRequest;
import com.buffetrestaurant.dto.response.MenuItemResponse;
import com.buffetrestaurant.dto.response.OrderResponse;
import com.buffetrestaurant.service.CustomerOrderingService;
import com.buffetrestaurant.service.impl.CustomerSessionAccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.CookieValue;
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
    private final CustomerOriginGuard originGuard;
    public CustomerOrderingController(CustomerOrderingService service, CustomerOriginGuard originGuard) {
        this.service = service;
        this.originGuard = originGuard;
    }

    @GetMapping("/dining-sessions/{sessionId}/menu")
    @Operation(summary = "Get available menu items allowed by the active session package",
            description = "Requires the customer_session cookie for the active session in the path")
    public List<MenuItemResponse> menu(@PathVariable Long sessionId,
                                       @CookieValue(name = CustomerSessionAccessService.COOKIE_NAME, required = false)
                                       String token) {
        return service.getMenu(sessionId, token);
    }

    @PostMapping("/dining-sessions/{sessionId}/orders")
    @Operation(summary = "Place a customer order with initial RECEIVED status",
            description = "Requires the customer_session cookie and an allowed Origin")
    @ApiResponse(responseCode = "201", description = "Customer order created with RECEIVED status",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class)),
            headers = @Header(name = "Location", description = "Relative URI of the created resource",
                    schema = @Schema(type = "string", format = "uri-reference", example = "/api/v1/dining-sessions/123/orders/456")))
    public ResponseEntity<OrderResponse> place(@PathVariable Long sessionId,
                                               @CookieValue(name = CustomerSessionAccessService.COOKIE_NAME, required = false)
                                               String token,
                                               @RequestHeader(name = "Origin", required = false) String origin,
                                               @Valid @RequestBody PlaceOrderRequest request) {
        originGuard.requireAllowed(origin);
        OrderResponse created = service.placeOrder(sessionId, token, request);
        return ResponseEntity.created(URI.create(ApiPaths.API_V1 + "/dining-sessions/" + sessionId
                + "/orders/" + created.orderId())).body(created);
    }

    @GetMapping("/dining-sessions/{sessionId}/orders")
    @Operation(summary = "Get orders for an active session; requires the customer cookie")
    public List<OrderResponse> orders(@PathVariable Long sessionId,
                                     @CookieValue(name = CustomerSessionAccessService.COOKIE_NAME, required = false)
                                     String token) {
        return service.getOrders(sessionId, token);
    }

    @GetMapping("/dining-sessions/{sessionId}/orders/{orderId}")
    @Operation(summary = "Get one order for an active session; requires the customer cookie")
    public OrderResponse order(@PathVariable Long sessionId, @PathVariable Long orderId,
                               @CookieValue(name = CustomerSessionAccessService.COOKIE_NAME, required = false)
                               String token) {
        return service.getOrder(sessionId, token, orderId);
    }
}
