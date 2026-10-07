package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buffetrestaurant.domain.StockItem;
import com.buffetrestaurant.domain.UserAccount;
import com.buffetrestaurant.domain.UserProfile;
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
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "app.menu.admin-access-provider=session")
class StockProfileFinalIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private AuthService authService;
    @Autowired private StockItemRepository stockItems;
    @Autowired private StockTransactionRepository transactions;
    @Autowired private UserAccountRepository users;
    @Autowired private UserProfileRepository profiles;
    @Autowired private org.springframework.jdbc.core.JdbcTemplate jdbc;

    private MockHttpSession manager;
    private MockHttpSession supervisor;
    private MockHttpSession kitchen;

    @BeforeEach
    void setUp() throws Exception {
        transactions.deleteAll();
        stockItems.deleteAll();
        profiles.deleteAll();
        users.deleteAll();
        createUser("manager", UserRole.MANAGER);
        createUser("supervisor", UserRole.SUPERVISOR);
        createUser("kitchen", UserRole.KITCHEN_STAFF);
        manager = login("manager");
        supervisor = login("supervisor");
        kitchen = login("kitchen");
    }

    @Test
    void createDefaultsTargetToZeroAndActiveTrueWithoutStockInOrQuantityChange() throws Exception {
        json(post("/api/v1/stock/items").session(manager), "{\"sku\":\"T-1\",\"name\":\"Rice\",\"unit\":\"kg\",\"lowStockThreshold\":2}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.openingTargetStock").value(0))
                .andExpect(jsonPath("$.shortfall").value(0))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.quantity").value(0));
        assertThat(transactions.count()).isZero();
    }

    @Test
    void targetIsShownWithShortfallAndNeverTouchesQuantityThresholdOrHistory() throws Exception {
        StockItem item = stockItems.save(new StockItem("T-2", "Milk", "l", new BigDecimal("3.000"), new BigDecimal("1.000")));
        json(put("/api/v1/stock/items/" + item.getId()).session(manager),
                "{\"sku\":\"T-2\",\"name\":\"Milk\",\"unit\":\"l\",\"lowStockThreshold\":1,\"openingTargetStock\":10.5}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openingTargetStock").value(10.5))
                .andExpect(jsonPath("$.shortfall").value(7.5))
                .andExpect(jsonPath("$.quantity").value(3.0));
        StockItem saved = stockItems.findById(item.getId()).orElseThrow();
        assertThat(saved.getQuantity()).isEqualByComparingTo("3.000");
        assertThat(saved.getLowStockThreshold()).isEqualByComparingTo("1.000");
        assertThat(transactions.count()).isZero();

        json(put("/api/v1/stock/items/" + item.getId()).session(manager),
                "{\"sku\":\"T-2\",\"name\":\"Milk\",\"unit\":\"l\",\"lowStockThreshold\":1,\"openingTargetStock\":2}")
                .andExpect(jsonPath("$.shortfall").value(0));
        mockMvc.perform(get("/api/v1/stock").session(supervisor)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].openingTargetStock").value(2));
    }

    @Test
    void rejectsInvalidTargetAndDuplicateSku() throws Exception {
        json(post("/api/v1/stock/items").session(manager),
                "{\"sku\":\"T-3\",\"name\":\"X\",\"unit\":\"kg\",\"lowStockThreshold\":1,\"openingTargetStock\":-1}")
                .andExpect(status().isBadRequest());
        json(post("/api/v1/stock/items").session(manager),
                "{\"sku\":\"T-3\",\"name\":\"X\",\"unit\":\"kg\",\"lowStockThreshold\":1,\"openingTargetStock\":1.0001}")
                .andExpect(status().isBadRequest());
        json(post("/api/v1/stock/items").session(manager),
                "{\"sku\":\"T-3\",\"name\":\"X\",\"unit\":\"kg\",\"lowStockThreshold\":1,\"openingTargetStock\":1}")
                .andExpect(status().isCreated());
        json(post("/api/v1/stock/items").session(manager),
                "{\"sku\":\"T-3\",\"name\":\"Y\",\"unit\":\"kg\",\"lowStockThreshold\":1}")
                .andExpect(status().isConflict());
    }

    @Test
    void inactiveItemKeepsHistoryButRejectsStockInAndAdjustmentUntilReactivated() throws Exception {
        StockItem item = stockItems.save(new StockItem("T-4", "Pork", "kg", new BigDecimal("5.000"), BigDecimal.ONE));
        String base = "/api/v1/stock/" + item.getId();
        json(post(base + "/in").session(supervisor), "{\"quantity\":2,\"reason\":\"delivery\"}").andExpect(status().isOk());

        json(put(base.replace("/stock/", "/stock/items/") + "/active").session(manager), "{\"active\":false}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.active").value(false));

        json(post(base + "/in").session(supervisor), "{\"quantity\":2,\"reason\":\"late\"}").andExpect(status().isConflict());
        json(post(base + "/adjustments").session(manager), "{\"quantityDelta\":-1,\"reason\":\"count\"}")
                .andExpect(status().isConflict());
        assertThat(stockItems.findById(item.getId()).orElseThrow().getQuantity()).isEqualByComparingTo("7.000");
        assertThat(transactions.count()).isEqualTo(1);
        mockMvc.perform(get("/api/v1/stock/transactions?itemId=" + item.getId()).session(supervisor))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(get("/api/v1/stock").session(supervisor)).andExpect(jsonPath("$[0].active").value(false));

        json(put(base.replace("/stock/", "/stock/items/") + "/active").session(manager), "{\"active\":true}")
                .andExpect(status().isOk());
        json(post(base + "/in").session(supervisor), "{\"quantity\":2,\"reason\":\"after reactivation\"}")
                .andExpect(status().isOk());
        assertThat(transactions.count()).isEqualTo(2);
    }

    @Test
    void onlyManagerMayCreateUpdateOrToggleStockItems() throws Exception {
        StockItem item = stockItems.save(new StockItem("T-5", "Egg", "pcs", BigDecimal.ONE, BigDecimal.ONE));
        String active = "/api/v1/stock/items/" + item.getId() + "/active";
        for (MockHttpSession session : new MockHttpSession[] {supervisor, kitchen}) {
            json(put(active).session(session), "{\"active\":false}").andExpect(status().isForbidden());
            json(post("/api/v1/stock/items").session(session),
                    "{\"sku\":\"T-6\",\"name\":\"X\",\"unit\":\"kg\",\"lowStockThreshold\":1}").andExpect(status().isForbidden());
        }
        json(put(active), "{\"active\":false}").andExpect(status().isUnauthorized());
        json(put(active).session(manager), "{}").andExpect(status().isBadRequest());
        json(put("/api/v1/stock/items/999999/active").session(manager), "{\"active\":false}").andExpect(status().isNotFound());
        assertThat(stockItems.findById(item.getId()).orElseThrow().isActive()).isTrue();
    }

    @Test
    void newAccountsRequireNamesAndLimitPhoneLength() throws Exception {
        String base = "{\"username\":\"%s\",\"password\":\"password123\",\"displayName\":\"Disp\",\"role\":\"SERVICE_STAFF\"%s}";
        json(post("/api/v1/admin/users").session(manager), base.formatted("u1", "")).andExpect(status().isBadRequest());
        json(post("/api/v1/admin/users").session(manager), base.formatted("u1", ",\"firstName\":\"A\"")).andExpect(status().isBadRequest());
        json(post("/api/v1/admin/users").session(manager),
                base.formatted("u1", ",\"firstName\":\"" + "a".repeat(101) + "\",\"lastName\":\"B\"")).andExpect(status().isBadRequest());
        json(post("/api/v1/admin/users").session(manager),
                base.formatted("u1", ",\"firstName\":\"A\",\"lastName\":\"B\",\"phoneNumber\":\"" + "1".repeat(21) + "\"")).andExpect(status().isBadRequest());
        json(post("/api/v1/admin/users").session(manager),
                base.formatted("u1", ",\"firstName\":\"  Ann \",\"lastName\":\"Lee\",\"phoneNumber\":\"+66 81-234-5678\""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Ann"))
                .andExpect(jsonPath("$.lastName").value("Lee"))
                .andExpect(jsonPath("$.phoneNumber").value("+66 81-234-5678"))
                .andExpect(jsonPath("$.displayName").value("Disp"));
        json(post("/api/v1/admin/users").session(manager),
                base.formatted("u2", ",\"firstName\":\"A\",\"lastName\":\"B\",\"phoneNumber\":\"abc\""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.phoneNumber").value("abc"));
        json(post("/api/v1/admin/users").session(manager),
                base.formatted("u1", ",\"firstName\":\"A\",\"lastName\":\"B\"")).andExpect(status().isConflict());
    }

    @Test
    void legacyProfileKeepsDisplayNameAndManagerCanFillNames() throws Exception {
        createUser("legacy", UserRole.SERVICE_STAFF);
        UserAccount legacy = users.findByUsername("legacy").orElseThrow();
        jdbc.update("UPDATE user_profiles SET display_name = ?, email = ?, first_name = NULL, last_name = NULL, phone_number = NULL WHERE user_id = ?",
                "Old Display", "old@example.test", legacy.getId());

        mockMvc.perform(get("/api/v1/admin/users").session(manager)).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.username=='legacy')].displayName").value("Old Display"))
                .andExpect(jsonPath("$[?(@.username=='legacy')].firstName").value((Object) null))
                .andExpect(jsonPath("$[?(@.username=='legacy')].phoneNumber").value((Object) null));

        String path = "/api/v1/admin/users/" + legacy.getId() + "/profile";
        json(put(path).session(supervisor), "{\"firstName\":\"A\",\"lastName\":\"B\"}").andExpect(status().isForbidden());
        json(put(path).session(manager), "{\"firstName\":\"\",\"lastName\":\"B\"}").andExpect(status().isBadRequest());
        json(put(path).session(manager), "{\"firstName\":\"A\",\"lastName\":\"B\",\"phoneNumber\":\"" + "1".repeat(21) + "\"}").andExpect(status().isBadRequest());
        json(put("/api/v1/admin/users/999999/profile").session(manager), "{\"firstName\":\"A\",\"lastName\":\"B\"}")
                .andExpect(status().isNotFound());
        json(put(path).session(manager), "{\"firstName\":\"Somchai\",\"lastName\":\"Jaidee\",\"phoneNumber\":\"0812345678\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Somchai"))
                .andExpect(jsonPath("$.displayName").value("Old Display"))
                .andExpect(jsonPath("$.email").value("old@example.test"));
        json(put(path).session(manager), "{\"firstName\":\"Somchai\",\"lastName\":\"Jaidee\",\"phoneNumber\":\"x1\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phoneNumber").value("x1"));
        UserProfile reloaded = profiles.findByUserId(legacy.getId()).orElseThrow();
        assertThat(reloaded.getLastName()).isEqualTo("Jaidee");
        assertThat(reloaded.getDisplayName()).isEqualTo("Old Display");
    }

    private ResultActions json(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder builder, String body)
            throws Exception {
        return mockMvc.perform(builder.contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private void createUser(String username, UserRole role) {
        authService.createUser(new CreateUserRequest(username, "password123", username, null, role, "First", "Last", null));
    }

    private MockHttpSession login(String username) throws Exception {
        return (MockHttpSession) mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"password\":\"password123\"}"))
                .andExpect(status().isOk()).andReturn().getRequest().getSession(false);
    }
}
