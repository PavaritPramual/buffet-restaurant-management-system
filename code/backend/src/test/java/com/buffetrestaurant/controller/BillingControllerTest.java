package com.buffetrestaurant.controller;

import com.buffetrestaurant.config.BillingConfig;
import com.buffetrestaurant.domain.enums.DiningSessionStatus;
import com.buffetrestaurant.dto.billing.BillingContext;
import com.buffetrestaurant.exception.GlobalExceptionHandler;
import com.buffetrestaurant.exception.ResourceNotFoundException;
import com.buffetrestaurant.service.billing.BillingContextProvider;
import com.buffetrestaurant.service.billing.BillingPreviewService;
import com.buffetrestaurant.service.fixture.DisabledBillingContextProvider;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BillingController.class)
@Import({GlobalExceptionHandler.class, BillingPreviewService.class, BillingConfig.class})
class BillingControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean BillingContextProvider provider;

    private BillingContext context() {
        BillingContext context = new BillingContext();
        context.setSessionId(12L);
        context.setPackagePrice(new BigDecimal("399.00"));
        context.setAdultCount(2);
        context.setChildCount(1);
        context.setSessionStatus(DiningSessionStatus.ACTIVE);
        return context;
    }

    @Test
    void computesFromProviderAndIgnoresBrowserPricing() throws Exception {
        when(provider.findBySessionId(12L)).thenReturn(context());
        mvc.perform(post("/api/v1/billing/preview").contentType(MediaType.APPLICATION_JSON)
                .content("{\"sessionId\":12,\"packagePrice\":1,\"adultCount\":0,\"childCount\":0,"
                        + "\"discountContext\":{\"percentage\":100},\"sessionStatus\":\"COMPLETED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(12))
                .andExpect(jsonPath("$.subtotalNoneDiscount").value(997.5))
                .andExpect(jsonPath("$.discountAmount").value(0))
                .andExpect(jsonPath("$.totalAmount").value(997.5));
        verify(provider).findBySessionId(12L);
    }

    @Test
    void backendDiscountAndRoundingArePreserved() throws Exception {
        BillingContext context = context();
        context.setPackagePrice(new BigDecimal("0.05"));
        context.setAdultCount(1);
        context.setChildCount(0);
        context.setDiscountContext(Map.of("percentage", 10));
        when(provider.findBySessionId(12L)).thenReturn(context);
        mvc.perform(post("/api/v1/billing/preview").contentType(MediaType.APPLICATION_JSON)
                .content("{\"sessionId\":12}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.discountAmount").value(0.005))
                .andExpect(jsonPath("$.totalBeforeRounding").value(0.045))
                .andExpect(jsonPath("$.roundingAdjustment").value(0.005))
                .andExpect(jsonPath("$.totalAmount").value(0.05));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"sessionId\":null}", "{\"sessionId\":0}", "{\"sessionId\":-1}"})
    void invalidIdsDoNotReachProvider(String json) throws Exception {
        mvc.perform(post("/api/v1/billing/preview").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.path").value("/api/v1/billing/preview"));
        verifyNoInteractions(provider);
    }

    @ParameterizedTest
    @ValueSource(strings = {"COMPLETED", "CANCELLED"})
    void doesNotCalculateInactiveSessions(String status) throws Exception {
        BillingContext context = context();
        context.setSessionStatus(DiningSessionStatus.valueOf(status));
        when(provider.findBySessionId(12L)).thenReturn(context);
        mvc.perform(post("/api/v1/billing/preview").contentType(MediaType.APPLICATION_JSON)
                .content("{\"sessionId\":12}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refusesContextForAnotherSession() throws Exception {
        BillingContext context = context();
        context.setSessionId(99L);
        when(provider.findBySessionId(12L)).thenReturn(context);
        mvc.perform(post("/api/v1/billing/preview").contentType(MediaType.APPLICATION_JSON)
                .content("{\"sessionId\":12}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingSessionReturns404() throws Exception {
        when(provider.findBySessionId(12L)).thenThrow(new ResourceNotFoundException("Session not found"));
        mvc.perform(post("/api/v1/billing/preview").contentType(MediaType.APPLICATION_JSON)
                .content("{\"sessionId\":12}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void disabledProviderReturns503InsteadOfInventingABill() throws Exception {
        when(provider.findBySessionId(12L)).thenAnswer(invocation ->
                new DisabledBillingContextProvider().findBySessionId(invocation.getArgument(0)));
        mvc.perform(post("/api/v1/billing/preview").contentType(MediaType.APPLICATION_JSON)
                .content("{\"sessionId\":12}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }
}
