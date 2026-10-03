package com.buffetrestaurant.integration;

import com.buffetrestaurant.controller.AuthController;
import com.buffetrestaurant.domain.Payment;
import com.buffetrestaurant.domain.enums.PaymentMethod;
import com.buffetrestaurant.domain.enums.PaymentStatus;
import com.buffetrestaurant.domain.enums.UserRole;
import com.buffetrestaurant.dto.response.UserContext;
import com.buffetrestaurant.integration.payment.PaymentStatusLookup;
import com.buffetrestaurant.repository.PaymentRepository;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "app.billing.context-provider=database",
        "app.payment.status-provider=database"
})
@Transactional
class PaymentIntegrationTest {

    private static final long SESSION_ID = 918001L;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PaymentRepository payments;

    @Autowired
    private PaymentStatusLookup statusLookup;

    @Autowired
    private EntityManager entityManager;

    private MockHttpSession staffSession;

    @BeforeEach
    void setUp() {
        jdbc.update("""
                INSERT INTO restaurant_tables
                    (id, table_number, capacity, status)
                VALUES (918001, 'BILL-IT', 4, 'OCCUPIED')
                """);

        jdbc.update("""
                INSERT INTO buffet_packages (id, name, price, active)
                VALUES (918001, 'Billing test', 499.00, TRUE)
                """);

        jdbc.update("""
                INSERT INTO soups (id, name, active)
                VALUES (918001, 'Billing soup', TRUE)
                """);

        jdbc.update("""
                INSERT INTO dining_sessions
                    (id, table_id, package_id, soup_id,
                     adult_count, child_count, session_token,
                     status, package_price_at_open)
                VALUES
                    (918001, 918001, 918001, 918001,
                     2, 1, 'billing-integration-token', 'ACTIVE', 399.00)
                """);

        staffSession = new MockHttpSession();
        staffSession.setAttribute(
                AuthController.USER_CONTEXT_SESSION_KEY,
                new UserContext(1L, "staff-test", "Test Staff",
                        UserRole.SERVICE_STAFF)
        );
    }

    @Test
    void previewUsesOpeningPriceInsteadOfCurrentPackagePrice() throws Exception {
        mvc.perform(post("/api/v1/billing/preview")
                        .session(staffSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionId\":" + SESSION_ID + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(997.50));

        assertThat(payments.findBySessionId(SESSION_ID)).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(PaymentMethod.class)
    void recordsPaidPaymentAndExposesStatus(PaymentMethod method) throws Exception {
        mvc.perform(post("/api/v1/payments")
                        .session(staffSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentRequest(method)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sessionId").value(SESSION_ID))
                .andExpect(jsonPath("$.paymentMethod").value(method.name()))
                .andExpect(jsonPath("$.paymentStatus").value("PAID"))
                .andExpect(jsonPath("$.paidAt").isNotEmpty());

        Payment saved = payments.findBySessionId(SESSION_ID).orElseThrow();

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getAmount()).isEqualByComparingTo("997.50");
        assertThat(saved.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(saved.getPaidAt()).isNotNull();

        PaymentStatusLookup.PaymentVerification verification =
                statusLookup.findPaymentForSession(SESSION_ID);

        assertThat(verification.sessionId()).isEqualTo(SESSION_ID);
        assertThat(verification.status()).isEqualTo(PaymentStatus.PAID);
    }

    @Test
    void rejectsDuplicatePayment() throws Exception {
        mvc.perform(post("/api/v1/payments")
                        .session(staffSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentRequest(PaymentMethod.CASH)))
                .andExpect(status().isCreated());

        Long originalPaymentId = payments.findBySessionId(SESSION_ID)
                .orElseThrow().getId();

        mvc.perform(post("/api/v1/payments")
                        .session(staffSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentRequest(PaymentMethod.CARD)))
                .andExpect(status().isConflict());

        Payment saved = payments.findBySessionId(SESSION_ID).orElseThrow();
        assertThat(saved.getId()).isEqualTo(originalPaymentId);
        assertThat(saved.getPaymentMethod()).isEqualTo(PaymentMethod.CASH);
    }

    private String paymentRequest(PaymentMethod method) {
        return "{\"sessionId\":" + SESSION_ID
                + ",\"paymentMethod\":\"" + method.name() + "\"}";
    }

    @Test
    void rejectsUnknownPaymentMethod() throws Exception {
        mvc.perform(post("/api/v1/payments")
                        .session(staffSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionId\":" + SESSION_ID
                                + ",\"paymentMethod\":\"BITCOIN\"}"))
                .andExpect(status().isBadRequest());

        assertThat(payments.findBySessionId(SESSION_ID)).isEmpty();
    }

    @Test
    void rejectsPaymentForCompletedSession() throws Exception {
        jdbc.update(
                "UPDATE dining_sessions SET status = 'COMPLETED' WHERE id = ?",
                SESSION_ID
        );

        mvc.perform(post("/api/v1/payments")
                        .session(staffSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentRequest(PaymentMethod.CASH)))
                .andExpect(status().isBadRequest());

        assertThat(payments.findBySessionId(SESSION_ID)).isEmpty();
    }

    @Test
    void rejectsClosingWithoutPayment() throws Exception {
        mvc.perform(post("/api/v1/dining-sessions/" + SESSION_ID + "/close")
                        .session(staffSession))
                .andExpect(status().isBadRequest());

        assertThat(jdbc.queryForObject(
                "SELECT status FROM dining_sessions WHERE id = ?",
                String.class, SESSION_ID
        )).isEqualTo("ACTIVE");

        assertThat(jdbc.queryForObject(
                "SELECT status FROM restaurant_tables WHERE id = ?",
                String.class, 918001L
        )).isEqualTo("OCCUPIED");
    }

    @Test
    void closesPaidSessionAndMakesTableAvailable() throws Exception {
        mvc.perform(post("/api/v1/payments")
                        .session(staffSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentRequest(PaymentMethod.CASH)))
                .andExpect(status().isCreated());

        assertThat(jdbc.queryForObject(
                "SELECT status FROM dining_sessions WHERE id = ?",
                String.class, SESSION_ID
        )).isEqualTo("ACTIVE");

        mvc.perform(post("/api/v1/dining-sessions/" + SESSION_ID + "/close")
                        .session(staffSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionStatus").value("COMPLETED"));

        entityManager.flush();

        assertThat(jdbc.queryForObject(
                "SELECT status FROM dining_sessions WHERE id = ?",
                String.class, SESSION_ID
        )).isEqualTo("COMPLETED");

        assertThat(jdbc.queryForObject(
                "SELECT status FROM restaurant_tables WHERE id = ?",
                String.class, 918001L
        )).isEqualTo("AVAILABLE");
    }
}