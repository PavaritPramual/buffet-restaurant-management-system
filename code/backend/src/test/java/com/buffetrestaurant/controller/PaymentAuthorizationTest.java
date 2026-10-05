package com.buffetrestaurant.controller;

import com.buffetrestaurant.config.BillingConfig;
import com.buffetrestaurant.domain.enums.UserRole;
import com.buffetrestaurant.dto.response.UserContext;
import com.buffetrestaurant.exception.GlobalExceptionHandler;
import com.buffetrestaurant.repository.PaymentRepository;
import com.buffetrestaurant.service.SessionPaymentAccessProvider;
import com.buffetrestaurant.service.SessionUserContextProvider;
import com.buffetrestaurant.service.billing.BillingContextProvider;
import com.buffetrestaurant.service.billing.BillingPreviewService;
import com.buffetrestaurant.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({BillingController.class, PaymentController.class})
@Import({
        GlobalExceptionHandler.class,
        BillingConfig.class,
        BillingPreviewService.class,
        PaymentServiceImpl.class,
        SessionPaymentAccessProvider.class,
        SessionUserContextProvider.class
})
class PaymentAuthorizationTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private BillingContextProvider contextProvider;

    @MockitoBean
    private PaymentRepository paymentRepository;

    @Test
    void rejectsRequestsWithoutLogin() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/v1/payments/sessions/12"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/billing/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionId\":12}"))
                .andExpect(status().isUnauthorized());

        mvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionId\":12,\"paymentMethod\":\"CASH\"}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(contextProvider, paymentRepository);
    }

    @ParameterizedTest
    @EnumSource(
            value = UserRole.class,
            names = {"KITCHEN_STAFF", "SUPERVISOR", "MANAGER"}
    )
    void rejectsRolesWithoutPaymentAccess(UserRole role) throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(
                AuthController.USER_CONTEXT_SESSION_KEY,
                new UserContext(1L, "test-user", "Test User", role)
        );

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/v1/payments/sessions/12").session(session))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/v1/billing/preview")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionId\":12}"))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/v1/payments")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionId\":12,\"paymentMethod\":\"CASH\"}"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(contextProvider, paymentRepository);
    }
}
