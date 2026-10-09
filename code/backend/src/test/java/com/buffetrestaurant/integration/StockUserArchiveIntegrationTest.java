package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buffetrestaurant.domain.StockItem;
import com.buffetrestaurant.domain.enums.UserRole;
import com.buffetrestaurant.dto.request.CreateUserRequest;
import com.buffetrestaurant.repository.StockItemRepository;
import com.buffetrestaurant.repository.StockTransactionRepository;
import com.buffetrestaurant.repository.UserAccountRepository;
import com.buffetrestaurant.repository.UserProfileRepository;
import com.buffetrestaurant.dto.response.UserContext;
import com.buffetrestaurant.exception.ResourceConflictException;
import com.buffetrestaurant.service.AuthService;
import com.buffetrestaurant.service.UserLifecycleService;
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
class StockUserArchiveIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private AuthService authService;
    @Autowired private UserLifecycleService lifecycle;
    @Autowired private StockItemRepository stockItems;
    @Autowired private StockTransactionRepository transactions;
    @Autowired private UserAccountRepository users;
    @Autowired private UserProfileRepository profiles;
    @Autowired private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        transactions.deleteAll();
        stockItems.deleteAll();
        profiles.deleteAll();
        users.deleteAll();
        createUser("manager", UserRole.MANAGER);
        createUser("manager2", UserRole.MANAGER);
        createUser("supervisor", UserRole.SUPERVISOR);
        createUser("kitchen", UserRole.KITCHEN_STAFF);
        createUser("staff", UserRole.SERVICE_STAFF);
    }

    @Test
    void neverUsedZeroQuantityItemIsHardDeleted() throws Exception {
        StockItem item = stockItems.save(new StockItem("DEL-1", "Unused", "kg", BigDecimal.ZERO, BigDecimal.ONE));
        mockMvc.perform(delete("/api/v1/stock/items/" + item.getId()).session(login("manager")))
                .andExpect(status().isNoContent());
        assertThat(stockItems.findById(item.getId())).isEmpty();
        mockMvc.perform(delete("/api/v1/stock/items/" + item.getId()).session(login("manager")))
                .andExpect(status().isNotFound());
    }

    @Test
    void itemWithHistoryIsArchivedKeepingHistoryAndBlocksStockChanges() throws Exception {
        StockItem item = stockItems.save(new StockItem("ARC-1", "Rice", "kg", BigDecimal.ZERO, BigDecimal.ONE));
        MockHttpSession manager = login("manager");
        mockMvc.perform(post("/api/v1/stock/" + item.getId() + "/in").session(manager)
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":5,\"reason\":\"delivery\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/stock/items/" + item.getId()).session(manager)).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/v1/stock/items/" + item.getId()).session(manager)).andExpect(status().isNoContent());

        assertThat(transactions.existsByStockItemId(item.getId())).isTrue();
        StockItem archived = stockItems.findById(item.getId()).orElseThrow();
        assertThat(archived.isArchived()).isTrue();
        assertThat(archived.isActive()).isFalse();
        assertThat(archived.getQuantity()).isEqualByComparingTo("5");

        mockMvc.perform(get("/api/v1/stock").session(manager)).andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(get("/api/v1/stock/items/archived").session(manager))
                .andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].archivedAt").exists());
        mockMvc.perform(get("/api/v1/stock/transactions").session(manager))
                .andExpect(jsonPath("$.length()").value(1));

        String stockIn = "{\"quantity\":1,\"reason\":\"again\"}";
        mockMvc.perform(post("/api/v1/stock/" + item.getId() + "/in").session(manager)
                .contentType(MediaType.APPLICATION_JSON).content(stockIn)).andExpect(status().isConflict());
        mockMvc.perform(post("/api/v1/stock/" + item.getId() + "/adjustments").session(manager)
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantityDelta\":-1,\"reason\":\"fix\"}"))
                .andExpect(status().isConflict());
        mockMvc.perform(put("/api/v1/stock/items/" + item.getId() + "/active").session(manager)
                .contentType(MediaType.APPLICATION_JSON).content("{\"active\":true}"))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/v1/stock/items/" + item.getId() + "/restore").session(manager))
                .andExpect(status().isOk()).andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.archivedAt").doesNotExist());
        mockMvc.perform(post("/api/v1/stock/items/" + item.getId() + "/restore").session(manager))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/stock/" + item.getId() + "/in").session(manager)
                .contentType(MediaType.APPLICATION_JSON).content(stockIn)).andExpect(status().isConflict());
    }

    @Test
    void legacyItemWithQuantityButNoTransactionsIsArchivedNotEdited() throws Exception {
        StockItem item = stockItems.save(new StockItem("LEG-1", "Legacy", "kg", new BigDecimal("7.000"),
                BigDecimal.ONE, new BigDecimal("20.000")));
        mockMvc.perform(delete("/api/v1/stock/items/" + item.getId()).session(login("manager")))
                .andExpect(status().isNoContent());
        StockItem after = stockItems.findById(item.getId()).orElseThrow();
        assertThat(after.isArchived()).isTrue();
        assertThat(after.getQuantity()).isEqualByComparingTo("7");
        assertThat(after.getOpeningTargetStock()).isEqualByComparingTo("20");
    }

    @Test
    void onlyManagerCanRemoveOrListArchivedStockAndUsers() throws Exception {
        StockItem item = stockItems.save(new StockItem("RBAC-1", "Item", "kg", BigDecimal.ZERO, BigDecimal.ONE));
        Long userId = users.findByUsername("staff").orElseThrow().getId();
        for (String name : new String[] {"supervisor", "kitchen", "staff"}) {
            MockHttpSession session = login(name);
            mockMvc.perform(delete("/api/v1/stock/items/" + item.getId()).session(session)).andExpect(status().isForbidden());
            mockMvc.perform(get("/api/v1/stock/items/archived").session(session)).andExpect(status().isForbidden());
            mockMvc.perform(post("/api/v1/stock/items/" + item.getId() + "/restore").session(session)).andExpect(status().isForbidden());
            mockMvc.perform(delete("/api/v1/admin/users/" + userId).session(session)).andExpect(status().isForbidden());
            mockMvc.perform(get("/api/v1/admin/users/archived").session(session)).andExpect(status().isForbidden());
            mockMvc.perform(post("/api/v1/admin/users/" + userId + "/restore").session(session)).andExpect(status().isForbidden());
            mockMvc.perform(put("/api/v1/admin/users/" + userId + "/active").session(session)
                    .contentType(MediaType.APPLICATION_JSON).content("{\"active\":false}")).andExpect(status().isForbidden());
        }
        mockMvc.perform(delete("/api/v1/stock/items/" + item.getId()).header("X-User-Role", "MANAGER"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/v1/admin/users/" + userId).header("X-User-Role", "MANAGER"))
                .andExpect(status().isUnauthorized());
        assertThat(stockItems.findById(item.getId())).isPresent();
        assertThat(users.findById(userId)).isPresent();
    }

    @Test
    void unreferencedUserIsHardDeletedAndSessionRevoked() throws Exception {
        MockHttpSession staff = login("staff");
        Long staffId = users.findByUsername("staff").orElseThrow().getId();
        mockMvc.perform(get("/api/v1/auth/me").session(staff)).andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/admin/users/" + staffId).session(login("manager")))
                .andExpect(status().isNoContent());

        assertThat(users.findById(staffId)).isEmpty();
        assertThat(profiles.findByUserId(staffId)).isEmpty();
        mockMvc.perform(delete("/api/v1/admin/users/" + staffId).session(login("manager")))
                .andExpect(status().isNotFound());
        assertThat(staff.isInvalid()).isTrue();
    }

    @Test
    void userWithStockHistoryIsArchivedLosesAccessAndKeepsActorName() throws Exception {
        StockItem item = stockItems.save(new StockItem("ACT-1", "Oil", "l", BigDecimal.ZERO, BigDecimal.ONE));
        MockHttpSession supervisor = login("supervisor");
        mockMvc.perform(post("/api/v1/stock/" + item.getId() + "/in").session(supervisor)
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":3,\"reason\":\"delivery\"}"))
                .andExpect(status().isOk());
        Long supervisorId = users.findByUsername("supervisor").orElseThrow().getId();
        MockHttpSession manager = login("manager");

        mockMvc.perform(delete("/api/v1/admin/users/" + supervisorId).session(manager)).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/v1/admin/users/" + supervisorId).session(manager)).andExpect(status().isNoContent());

        assertThat(users.findById(supervisorId).orElseThrow().isArchived()).isTrue();
        assertThat(supervisor.isInvalid()).isTrue();
        mockMvc.perform(get("/api/v1/stock").session(supervisor)).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"supervisor\",\"password\":\"password123\"}")).andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/stock/transactions").session(manager))
                .andExpect(jsonPath("$.length()").value(1));
        assertThat(jdbc.queryForObject("select count(*) from stock_transactions where actor_user_id = ?",
                Integer.class, supervisorId)).isEqualTo(1);
        mockMvc.perform(get("/api/v1/admin/users").session(manager)).andExpect(jsonPath("$.length()").value(4));
        mockMvc.perform(get("/api/v1/admin/users/archived").session(manager))
                .andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].username").value("supervisor"));

        mockMvc.perform(post("/api/v1/admin/users/" + supervisorId + "/restore").session(manager))
                .andExpect(status().isOk()).andExpect(jsonPath("$.active").value(false));
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"supervisor\",\"password\":\"password123\"}")).andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/v1/admin/users/" + supervisorId + "/active").session(manager)
                .contentType(MediaType.APPLICATION_JSON).content("{\"active\":true}")).andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"supervisor\",\"password\":\"password123\"}")).andExpect(status().isOk());
    }

    @Test
    void archivedUserCannotBeEnabledWithoutRestore() throws Exception {
        StockItem item = stockItems.save(new StockItem("ACT-2", "Salt", "kg", BigDecimal.ZERO, BigDecimal.ONE));
        MockHttpSession kitchen = login("kitchen");
        MockHttpSession manager = login("manager");
        Long kitchenId = users.findByUsername("kitchen").orElseThrow().getId();
        mockMvc.perform(post("/api/v1/stock/" + item.getId() + "/in").session(manager)
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":1,\"reason\":\"d\"}"));
        jdbc.update("update stock_transactions set actor_user_id = ?", kitchenId);
        mockMvc.perform(delete("/api/v1/admin/users/" + kitchenId).session(manager)).andExpect(status().isNoContent());
        mockMvc.perform(put("/api/v1/admin/users/" + kitchenId + "/active").session(manager)
                .contentType(MediaType.APPLICATION_JSON).content("{\"active\":true}")).andExpect(status().isConflict());
        assertThat(kitchen.isInvalid()).isTrue();
    }

    @Test
    void cannotRemoveOrDisableSelf() throws Exception {
        Long managerId = users.findByUsername("manager").orElseThrow().getId();
        MockHttpSession manager = login("manager");
        mockMvc.perform(delete("/api/v1/admin/users/" + managerId).session(manager)).andExpect(status().isConflict());
        mockMvc.perform(put("/api/v1/admin/users/" + managerId + "/active").session(manager)
                .contentType(MediaType.APPLICATION_JSON).content("{\"active\":false}")).andExpect(status().isConflict());
        assertThat(users.findById(managerId).orElseThrow().isActive()).isTrue();
        mockMvc.perform(get("/api/v1/auth/me").session(manager)).andExpect(status().isOk());
    }

    @Test
    void lastActiveManagerCannotBeRemovedOrDisabled() throws Exception {
        Long manager2Id = users.findByUsername("manager2").orElseThrow().getId();
        Long managerId = users.findByUsername("manager").orElseThrow().getId();
        MockHttpSession manager = login("manager");
        MockHttpSession manager2 = login("manager2");

        mockMvc.perform(delete("/api/v1/admin/users/" + manager2Id).session(manager)).andExpect(status().isNoContent());
        assertThat(manager2.isInvalid()).isTrue();

        // an actor id that is not the target, so only the last-Manager rule can stop it
        UserContext otherActor = new UserContext(-1L, "ghost", "Ghost", null);
        assertThatThrownBy(() -> lifecycle.remove(managerId, otherActor)).isInstanceOf(ResourceConflictException.class);
        assertThatThrownBy(() -> lifecycle.setActive(managerId, false, otherActor))
                .isInstanceOf(ResourceConflictException.class);
        assertThat(users.findById(managerId).orElseThrow().isActive()).isTrue();
        assertThat(users.findById(managerId).orElseThrow().isArchived()).isFalse();
        mockMvc.perform(get("/api/v1/auth/me").session(manager)).andExpect(status().isOk());
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
