package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buffetrestaurant.domain.StockItem;
import com.buffetrestaurant.domain.enums.UserRole;
import com.buffetrestaurant.dto.request.CreateUserRequest;
import com.buffetrestaurant.repository.StockItemRepository;
import com.buffetrestaurant.repository.StockTransactionRepository;
import com.buffetrestaurant.repository.UserAccountRepository;
import com.buffetrestaurant.repository.UserProfileRepository;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "app.menu.admin-access-provider=session")
class AuthStockIntegrationTest {
    @Autowired private MockMvc mockMvc;
        @Autowired private AuthService authService;
    @Autowired private StockItemRepository stockItems;
    @Autowired private StockTransactionRepository transactions;
        @Autowired private UserAccountRepository users;
        @Autowired private UserProfileRepository profiles;
        @Autowired private JdbcTemplate jdbcTemplate;

    private StockItem stockItem;

    @BeforeEach
    void setUp() {
        transactions.deleteAll();
        stockItems.deleteAll();
        profiles.deleteAll();
        users.deleteAll();
        createUser("staff", UserRole.SERVICE_STAFF);
        createUser("kitchen", UserRole.KITCHEN_STAFF);
        createUser("supervisor", UserRole.SUPERVISOR);
        createUser("manager", UserRole.MANAGER);
        stockItem = stockItems.save(new StockItem("ING-INT", "Rice", "kg",
                new BigDecimal("10.000"), new BigDecimal("2.000")));
    }

    @Test
    void rotatesSessionIdAfterSuccessfulLogin() throws Exception {
        MockHttpSession anonymousSession = new MockHttpSession();
        String anonymousId = anonymousSession.getId();

        MockHttpSession authenticatedSession = (MockHttpSession) mockMvc.perform(post("/api/v1/auth/login")
                .session(anonymousSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"staff\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andReturn().getRequest().getSession(false);

        assertNotEquals(anonymousId, authenticatedSession.getId());
    }

        @Test
        void rejectsLoginValuesBeyondDtoBounds() throws Exception {
                mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                                .content("{\"username\":\"" + "u".repeat(81) + "\",\"password\":\"password123\"}"))
                                .andExpect(status().isBadRequest());
                mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                                .content("{\"username\":\"staff\",\"password\":\"" + "p".repeat(73) + "\"}"))
                                .andExpect(status().isBadRequest());
        }

    @Test
    void appliesRoleMatrixToUsersStockAndMenuMutations() throws Exception {
        MockHttpSession staff = login("staff");
                mockMvc.perform(get("/api/v1/stock").session(staff)).andExpect(status().isForbidden());
                mockMvc.perform(get("/api/v1/stock/transactions").session(staff)).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/admin/users").session(staff)).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/admin/users").session(staff).contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"blocked\",\"password\":\"password123\",\"displayName\":\"Blocked\",\"role\":\"MANAGER\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/stock/" + stockItem.getId() + "/in").session(staff)
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":1,\"reason\":\"delivery\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/menu-categories").session(staff)
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Staff category\"}"))
                .andExpect(status().isForbidden());

        MockHttpSession kitchen = login("kitchen");
        mockMvc.perform(get("/api/v1/stock").session(kitchen)).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/stock/transactions").session(kitchen)).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/stock/" + stockItem.getId() + "/adjustments").session(kitchen)
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantityDelta\":-1,\"reason\":\"count\"}"))
                .andExpect(status().isForbidden());

        MockHttpSession supervisor = login("supervisor");
        mockMvc.perform(get("/api/v1/stock").session(supervisor)).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/stock/transactions").session(supervisor)).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/admin/users").session(supervisor)).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/stock/" + stockItem.getId() + "/in").session(supervisor)
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":1,\"reason\":\"delivery\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/stock/" + stockItem.getId() + "/adjustments").session(supervisor)
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantityDelta\":-1,\"reason\":\"count\"}"))
                .andExpect(status().isOk());

        MockHttpSession manager = login("manager");
        mockMvc.perform(get("/api/v1/stock").session(manager)).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/stock/transactions").session(manager)).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/admin/users").session(manager)).andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/menu-categories").session(manager)
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Manager category\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/admin/users").session(manager).contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"new-staff\",\"password\":\"password123\",\"displayName\":\"New Staff\",\"role\":\"SERVICE_STAFF\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void userProfileEmailValidationMatchesOptionalFrontendEmailField() throws Exception {
        MockHttpSession manager = login("manager");
        mockMvc.perform(post("/api/v1/admin/users").session(manager).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"bad-email\",\"password\":\"password123\","
                                + "\"displayName\":\"Bad Email\",\"email\":\"not-an-email\",\"role\":\"SERVICE_STAFF\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.timestamp").value(org.hamcrest.Matchers.endsWith("Z")))
                .andExpect(jsonPath("$.path").value("/api/v1/admin/users"));

        mockMvc.perform(post("/api/v1/admin/users").session(manager).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"no-email\",\"password\":\"password123\","
                                + "\"displayName\":\"No Email\",\"email\":null,\"role\":\"SERVICE_STAFF\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.username").value("no-email"))
                .andExpect(jsonPath("$.displayName").value("No Email"))
                .andExpect(jsonPath("$.role").value("SERVICE_STAFF"));
    }

    @Test
    void loginStockInAdjustmentAndHistoryUseTheSameAuditTrail() throws Exception {
        mockMvc.perform(get("/api/v1/stock")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/stock/transactions")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"staff\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());

        MockHttpSession session = login("manager");
        mockMvc.perform(get("/api/v1/auth/me").session(session))
                .andExpect(jsonPath("$.role").value("MANAGER"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/stock/" + stockItem.getId() + "/in").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":2.500,\"reason\":\"Supplier delivery\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantityDelta").isNumber())
                .andExpect(jsonPath("$.balanceAfter").isNumber())
                .andExpect(jsonPath("$.createdAt").value(org.hamcrest.Matchers.endsWith("Z")))
                .andExpect(jsonPath("$.balanceAfter").value(12.5));
        mockMvc.perform(post("/api/v1/stock/" + stockItem.getId() + "/adjustments").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantityDelta\":-1.250,\"reason\":\"Spillage\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balanceAfter").value(11.25));

        mockMvc.perform(post("/api/v1/stock/" + stockItem.getId() + "/in").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":0,\"reason\":\"Invalid receipt\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/v1/stock/" + stockItem.getId() + "/adjustments").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantityDelta\":-30,\"reason\":\"Count correction\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/v1/stock/" + stockItem.getId() + "/adjustments").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantityDelta\":1,\"reason\":\"  \"}"))
                .andExpect(status().isBadRequest());

        assertThat(stockItems.findById(stockItem.getId()).orElseThrow().getQuantity())
                .isEqualByComparingTo("11.250");
        assertThat(transactions.findAllByStockItemIdOrderByCreatedAtDescIdDesc(stockItem.getId()))
                .hasSize(2);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM stock_transactions WHERE actor_user_id = (SELECT id FROM app_users WHERE username = 'manager')",
                Integer.class)).isEqualTo(2);
        mockMvc.perform(get("/api/v1/stock/transactions").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

        private void createUser(String username, UserRole role) {
                authService.createUser(new CreateUserRequest(username, "password123", username, null, role));
        }

        private MockHttpSession login(String username) throws Exception {
                return (MockHttpSession) mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"username\":\"" + username + "\",\"password\":\"password123\"}"))
                                .andExpect(status().isOk())
                                .andReturn().getRequest().getSession(false);
        }
}