package com.buffetrestaurant.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buffetrestaurant.domain.enums.OrderStatus;
import com.buffetrestaurant.dto.request.UpdateOrderStatusRequest;
import com.buffetrestaurant.dto.response.OrderResponse;
import com.buffetrestaurant.exception.BusinessRuleException;
import com.buffetrestaurant.exception.ForbiddenException;
import com.buffetrestaurant.exception.GlobalExceptionHandler;
import com.buffetrestaurant.exception.ResourceNotFoundException;
import com.buffetrestaurant.exception.UnauthorizedException;
import com.buffetrestaurant.service.OrderFulfillmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderFulfillmentController.class)
@Import(GlobalExceptionHandler.class)
class OrderFulfillmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderFulfillmentService fulfillmentService;

    private OrderResponse order(long id, OrderStatus status) {
        return new OrderResponse(id, 1L, "A01",
                List.of(new OrderResponse.Item(101L, "Sliced Pork", 2)),
                status, OffsetDateTime.parse("2026-09-18T10:00:00+07:00"));
    }

    @Test
    void incoming_whenCalled_returns200AndReceivedAndPreparingOrders() throws Exception {
        when(fulfillmentService.getIncomingOrders())
                .thenReturn(List.of(order(1, OrderStatus.RECEIVED), order(2, OrderStatus.PREPARING)));

        mockMvc.perform(get("/api/v1/orders/incoming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("RECEIVED"))
                .andExpect(jsonPath("$[1].status").value("PREPARING"));
    }

    @Test
    void ready_whenCalled_returns200AndReadyOrders() throws Exception {
        when(fulfillmentService.getReadyOrders()).thenReturn(List.of(order(3, OrderStatus.READY)));

        mockMvc.perform(get("/api/v1/orders/ready"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("READY"));
    }

    @Test
    void updateStatus_whenValidTransition_returns200AndUpdatedOrder() throws Exception {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.PREPARING);
        when(fulfillmentService.advanceStatus(eq(1L), eq(OrderStatus.PREPARING)))
                .thenReturn(order(1, OrderStatus.PREPARING));

        mockMvc.perform(patch("/api/v1/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.status").value("PREPARING"));
    }

    @Test
    void updateStatus_whenBodyMissingStatus_returns400AndErrorResponse() throws Exception {
        mockMvc.perform(patch("/api/v1/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/v1/orders/1/status"));
    }

    @Test
    void updateStatus_whenUnsupportedEnum_returns400AndErrorResponse() throws Exception {
        mockMvc.perform(patch("/api/v1/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"preparing\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Malformed request or unsupported enum value"))
                .andExpect(jsonPath("$.path").value("/api/v1/orders/1/status"));
    }

    @Test
    void updateStatus_whenSkippingState_returns400AndErrorResponse() throws Exception {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.READY);
        when(fulfillmentService.advanceStatus(eq(1L), eq(OrderStatus.READY)))
                .thenThrow(new BusinessRuleException(
                        "Cannot change order 1 status from RECEIVED to READY; the only allowed next status is PREPARING"));

        mockMvc.perform(patch("/api/v1/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(
                        "Cannot change order 1 status from RECEIVED to READY; the only allowed next status is PREPARING"))
                .andExpect(jsonPath("$.path").value("/api/v1/orders/1/status"));
    }

    @Test
    void updateStatus_whenOrderNotFound_returns404AndErrorResponse() throws Exception {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.PREPARING);
        when(fulfillmentService.advanceStatus(eq(999L), any()))
                .thenThrow(new ResourceNotFoundException("Order not found with id: 999"));

        mockMvc.perform(patch("/api/v1/orders/999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Order not found with id: 999"))
                .andExpect(jsonPath("$.path").value("/api/v1/orders/999/status"));
    }

    @Test
    void incoming_whenCallerNotAuthenticated_returns401AndErrorResponse() throws Exception {
        when(fulfillmentService.getIncomingOrders())
                .thenThrow(new UnauthorizedException("Missing X-User-Role header; caller is not authenticated"));

        mockMvc.perform(get("/api/v1/orders/incoming"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.path").value("/api/v1/orders/incoming"));
    }

    @Test
    void incoming_whenCallerIsServiceStaff_returns403AndErrorResponse() throws Exception {
        when(fulfillmentService.getIncomingOrders())
                .thenThrow(new ForbiddenException("Role SERVICE_STAFF is not permitted to perform this action"));

        mockMvc.perform(get("/api/v1/orders/incoming"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Role SERVICE_STAFF is not permitted to perform this action"));
    }

    @Test
    void ready_whenCallerNotAuthenticated_returns401AndErrorResponse() throws Exception {
        when(fulfillmentService.getReadyOrders())
                .thenThrow(new UnauthorizedException("Missing X-User-Role header; caller is not authenticated"));

        mockMvc.perform(get("/api/v1/orders/ready"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void ready_whenCallerIsKitchenStaff_returns403AndErrorResponse() throws Exception {
        when(fulfillmentService.getReadyOrders())
                .thenThrow(new ForbiddenException("Role KITCHEN_STAFF is not permitted to perform this action"));

        mockMvc.perform(get("/api/v1/orders/ready"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Role KITCHEN_STAFF is not permitted to perform this action"));
    }

    @Test
    void updateStatus_whenCallerNotAuthenticated_returns401AndErrorResponse() throws Exception {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.PREPARING);
        when(fulfillmentService.advanceStatus(eq(1L), eq(OrderStatus.PREPARING)))
                .thenThrow(new UnauthorizedException("Missing X-User-Role header; caller is not authenticated"));

        mockMvc.perform(patch("/api/v1/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void updateStatus_whenServiceStaffTriesToMarkPreparing_returns403AndErrorResponse() throws Exception {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.PREPARING);
        when(fulfillmentService.advanceStatus(eq(1L), eq(OrderStatus.PREPARING)))
                .thenThrow(new ForbiddenException("Role SERVICE_STAFF is not permitted to perform this action"));

        mockMvc.perform(patch("/api/v1/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Role SERVICE_STAFF is not permitted to perform this action"));
    }

    @Test
    void updateStatus_whenKitchenStaffTriesToMarkServed_returns403AndErrorResponse() throws Exception {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.SERVED);
        when(fulfillmentService.advanceStatus(eq(1L), eq(OrderStatus.SERVED)))
                .thenThrow(new ForbiddenException("Role KITCHEN_STAFF is not permitted to perform this action"));

        mockMvc.perform(patch("/api/v1/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Role KITCHEN_STAFF is not permitted to perform this action"));
    }
}
