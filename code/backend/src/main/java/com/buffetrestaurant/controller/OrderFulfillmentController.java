package com.buffetrestaurant.controller;

import com.buffetrestaurant.common.ApiPaths;
import com.buffetrestaurant.dto.request.UpdateOrderStatusRequest;
import com.buffetrestaurant.dto.response.OrderResponse;
import com.buffetrestaurant.service.OrderFulfillmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/orders")
@Tag(name = "Order Fulfillment", description = "Kitchen board and service staff serving workflow")
public class OrderFulfillmentController {

    private final OrderFulfillmentService fulfillmentService;

    public OrderFulfillmentController(OrderFulfillmentService fulfillmentService) {
        this.fulfillmentService = fulfillmentService;
    }

    @Operation(summary = "Get orders the kitchen still needs to act on (RECEIVED or PREPARING)")
    @GetMapping("/incoming")
    public List<OrderResponse> incoming() {
        return fulfillmentService.getIncomingOrders();
    }

    @Operation(summary = "Get orders ready for service staff to serve (READY)")
    @GetMapping("/ready")
    public List<OrderResponse> ready() {
        return fulfillmentService.getReadyOrders();
    }

    @Operation(summary = "Advance an order to the next fulfillment status")
    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return fulfillmentService.advanceStatus(id, request.status());
    }
}
