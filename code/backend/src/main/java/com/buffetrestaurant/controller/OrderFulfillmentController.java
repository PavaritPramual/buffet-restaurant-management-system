package com.buffetrestaurant.controller;

import com.buffetrestaurant.common.ApiPaths;
import com.buffetrestaurant.dto.request.UpdateOrderStatusRequest;
import com.buffetrestaurant.dto.response.ErrorResponse;
import com.buffetrestaurant.dto.response.OrderResponse;
import com.buffetrestaurant.service.OrderFulfillmentService;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@SecurityRequirement(name = "staffSessionCookie")
public class OrderFulfillmentController {

    private final OrderFulfillmentService fulfillmentService;

    public OrderFulfillmentController(OrderFulfillmentService fulfillmentService) {
        this.fulfillmentService = fulfillmentService;
    }

    @Operation(summary = "Get orders the kitchen still needs to act on (RECEIVED or PREPARING)",
            description = "Requires KITCHEN_STAFF; MANAGER and SUPERVISOR roles do not grant access.")
    @ApiResponse(responseCode = "200", description = "Incoming kitchen orders",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = OrderResponse.class))))
    @ApiResponse(responseCode = "401", description = "Staff login required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "KITCHEN_STAFF role required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/incoming")
    public List<OrderResponse> incoming() {
        return fulfillmentService.getIncomingOrders();
    }

    @Operation(summary = "Get orders ready for service staff to serve (READY)",
            description = "Requires SERVICE_STAFF; MANAGER and SUPERVISOR roles do not grant access.")
    @ApiResponse(responseCode = "200", description = "Orders ready for service",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = OrderResponse.class))))
    @ApiResponse(responseCode = "401", description = "Staff login required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "SERVICE_STAFF role required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/ready")
    public List<OrderResponse> ready() {
        return fulfillmentService.getReadyOrders();
    }

    @Operation(summary = "Advance an order to the next fulfillment status",
            description = "KITCHEN_STAFF may advance RECEIVED to PREPARING and PREPARING to READY. "
                    + "SERVICE_STAFF may advance READY to SERVED. No other role may transition an order. "
                    + "Starting PREPARING consumes the recipe frozen at order placement atomically; insufficient or inactive stock returns 409 without changing order or stock.")
    @ApiResponse(responseCode = "409", description = "Recipe stock is insufficient or unavailable; no partial consumption",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "400", description = "Transition is not the next legal status",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "200", description = "Order transitioned to its next legal status",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class)))
    @ApiResponse(responseCode = "401", description = "Staff login required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Role does not permit this transition",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Order not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return fulfillmentService.advanceStatus(id, request.status());
    }
}
