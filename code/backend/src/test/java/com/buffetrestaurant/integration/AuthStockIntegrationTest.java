package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buffetrestaurant.domain.StockItem;
import com.buffetrestaurant.domain.enums.UserRole;
import com.buffetrestaurant.dto.request.CreateUserRequest;
import com.buffetrestaurant.repository.StockItemRepository;
import com.buffetrestaurant.repository.StockTransactionRepository;
import com.buffetrestaurant.service.AuthService;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthStockIntegrationTest {
    @Autowired private MockMvc mockMvc;
        @Autowired private AuthService authService;
    @Autowired private StockItemRepository stockItems;
    @Autowired private StockTransactionRepository transactions;
        @Autowired private JdbcTemplate jdbcTemplate;

    private StockItem stockItem;

    @BeforeEach
    void setUp() {
        transactions.deleteAll();
        stockItems.deleteAll();
        authService.createUser(new CreateUserRequest("staff", "password123", "Service Staff",
                "staff@example.test", UserRole.SERVICE_STAFF));
        stockItem = stockItems.save(new StockItem("ING-INT", "Rice", "kg",
                new BigDecimal("10.000"), new BigDecimal("2.000")));
    }

    @Test
    void loginStockInAdjustmentAndHistoryUseTheSameAuditTrail() throws Exception {
        mockMvc.perform(get("/api/v1/stock")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"staff\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());

        MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"staff\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("SERVICE_STAFF"))
                .andReturn().getRequest().getSession(false);

        mockMvc.perform(post("/api/v1/stock/" + stockItem.getId() + "/in").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":\"2.500\",\"reason\":\"Supplier delivery\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balanceAfter").value(12.5));
        mockMvc.perform(post("/api/v1/stock/" + stockItem.getId() + "/adjustments").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantityDelta\":\"-1.250\",\"reason\":\"Spillage\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balanceAfter").value(11.25));

        mockMvc.perform(post("/api/v1/stock/" + stockItem.getId() + "/in").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":\"0\",\"reason\":\"Invalid receipt\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/v1/stock/" + stockItem.getId() + "/adjustments").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantityDelta\":\"-30\",\"reason\":\"Count correction\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/v1/stock/" + stockItem.getId() + "/adjustments").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantityDelta\":\"1\",\"reason\":\"  \"}"))
                .andExpect(status().isBadRequest());

        assertThat(stockItems.findById(stockItem.getId()).orElseThrow().getQuantity())
                .isEqualByComparingTo("11.250");
        assertThat(transactions.findAllByStockItemIdOrderByCreatedAtDescIdDesc(stockItem.getId()))
                .hasSize(2);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM stock_transactions WHERE actor_user_id = (SELECT id FROM app_users WHERE username = 'staff')",
                Integer.class)).isEqualTo(2);
        mockMvc.perform(get("/api/v1/stock/transactions").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}