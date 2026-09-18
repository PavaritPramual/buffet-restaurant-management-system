package com.buffetrestaurant.ordering;

import com.buffetrestaurant.ordering.OrderingModels.Order;
import com.buffetrestaurant.ordering.OrderingModels.OrderLine;
import com.buffetrestaurant.domain.enums.OrderStatus;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderingController.class)
class OrderingControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean OrderingService service;

    @Test
    void postOrder_whenQuantityIsZero_returns400() throws Exception {
        mvc.perform(post("/api/v1/dining-sessions/1/orders")
                .contentType("application/json")
                .content("{\"items\":[{\"menuItemId\":1,\"quantity\":0}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/v1/dining-sessions/1/orders"));
    }

    @Test
    void postOrder_whenValid_returns201AndReceivedOrder() throws Exception {
        Order order = new Order(7L, 1L, "A01", List.of(new OrderLine(1L, "หมูสไลซ์", 2)),
                OrderStatus.RECEIVED, OffsetDateTime.parse("2026-09-18T17:00:00+07:00"));
        when(service.place(eq(1L), any())).thenReturn(order);
        mvc.perform(post("/api/v1/dining-sessions/1/orders")
                .contentType("application/json")
                .content("{\"items\":[{\"menuItemId\":1,\"quantity\":2}]}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/orders/7"))
                .andExpect(jsonPath("$.status").value("RECEIVED"));
    }

    @Test
    void postMenuItem_whenImageUrlUsesUnsupportedScheme_returns400() throws Exception {
        mvc.perform(post("/api/v1/menu-items")
                .contentType("application/json")
                .content("{\"categoryId\":1,\"name\":\"Soup\",\"available\":true,"
                        + "\"packageIds\":[1],\"imageUrl\":\"javascript:alert(1)\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
