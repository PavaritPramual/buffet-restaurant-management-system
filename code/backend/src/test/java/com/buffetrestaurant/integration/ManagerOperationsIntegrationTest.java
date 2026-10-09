package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.buffetrestaurant.common.UserSessionKeys;
import com.buffetrestaurant.domain.enums.UserRole;
import com.buffetrestaurant.dto.response.UserContext;
import com.buffetrestaurant.service.impl.CustomerSessionAccessService;
import jakarta.servlet.http.Cookie;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"app.menu.admin-access-provider=session", "app.dining-session.staff-access-provider=session",
        "app.ordering.session-provider=database", "app.fulfillment.access-provider=session", "app.payment.status-provider=database"})
@Transactional
class ManagerOperationsIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired CustomerSessionAccessService customers;
    @Autowired jakarta.persistence.EntityManager entities;

    @BeforeEach void seed() {
        jdbc.update("INSERT INTO buffet_packages(id,name,price,active) VALUES (980001,'Override package',499,true)");
        jdbc.update("INSERT INTO soups(id,name,active) VALUES (980001,'Override soup',true)");
        jdbc.update("INSERT INTO restaurant_tables(id,table_number,capacity,status) VALUES (980001,'FORCE-01',4,'OCCUPIED')");
        jdbc.update("INSERT INTO dining_sessions(id,table_id,package_id,soup_id,adult_count,child_count,session_token,status,package_price_at_open) VALUES (980001,980001,980001,980001,1,0,'override-test-qr','ACTIVE',499)");
        jdbc.update("INSERT INTO menu_categories(id,name) VALUES (980001,'Override foods')");
        jdbc.update("INSERT INTO menu_items(id,category_id,name,available) VALUES (980001,980001,'History food',true)");
        jdbc.update("INSERT INTO package_menu_items(package_id,menu_item_id) VALUES (980001,980001)");
        jdbc.update("INSERT INTO orders(id,session_id,table_number,status) VALUES (980001,980001,'FORCE-01','RECEIVED')");
        jdbc.update("INSERT INTO order_items(id,order_id,menu_item_id,item_name,quantity) VALUES (980001,980001,980001,'Historical name',2)");
    }
    private MockHttpSession role(UserRole role) {
        var session = new MockHttpSession();
        session.setAttribute(UserSessionKeys.USER_CONTEXT_SESSION_KEY, new UserContext(980001L, "override-" + role, role.name(), role));
        return session;
    }
    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder force(String path) {
        return post("/api/v1/manager/" + path).contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"  test cleanup  \"}");
    }
    private Cookie customer() { return new Cookie("customer_session", customers.exchange("override-test-qr").credential()); }

    @Test void forceClosesUnpaidSessionRevokesQrAndCustomerStopsBoardsAndReopensTableWithoutFakePayment() throws Exception {
        Cookie cookie = customer();
        mvc.perform(post("/api/v1/dining-sessions/980001/close").session(role(UserRole.SERVICE_STAFF))).andExpect(status().isBadRequest());
        mvc.perform(force("dining-sessions/980001/force-close").session(role(UserRole.MANAGER)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.sessionStatus").value("CANCELLED"))
                .andExpect(jsonPath("$.endTime").isNotEmpty()).andExpect(jsonPath("$.sessionToken").doesNotExist());
        entities.flush();
        assertThat(jdbc.queryForObject("SELECT status FROM restaurant_tables WHERE id=980001", String.class)).isEqualTo("AVAILABLE");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM payments WHERE session_id=980001", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM customer_session_grants WHERE session_id=980001", Integer.class)).isZero();
        String remainingQr = jdbc.queryForObject("SELECT session_token FROM dining_sessions WHERE id=980001", String.class);
        mvc.perform(post("/api/v1/dining-sessions/qr-exchange").header("Origin", "http://localhost:5173")
                .contentType(MediaType.APPLICATION_JSON).content("{\"token\":\"" + remainingQr + "\"}"))
                .andExpect(status().isNotFound());
        assertThat(jdbc.queryForObject("SELECT item_name FROM order_items WHERE id=980001", String.class)).isEqualTo("Historical name");
        mvc.perform(get("/api/v1/dining-sessions/980001/menu").cookie(cookie)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/orders/incoming").session(role(UserRole.KITCHEN_STAFF))).andExpect(jsonPath("$[?(@.sessionId == 980001)]").isEmpty());
        mvc.perform(patch("/api/v1/orders/980001/status").session(role(UserRole.KITCHEN_STAFF)).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"PREPARING\"}"))
                .andExpect(status().isBadRequest());
        assertThat(jdbc.queryForObject("SELECT status FROM orders WHERE id=980001", String.class)).isEqualTo("RECEIVED");
        mvc.perform(force("dining-sessions/980001/force-close").session(role(UserRole.MANAGER))).andExpect(status().isBadRequest());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM manager_operations", Integer.class)).isEqualTo(1);
        mvc.perform(post("/api/v1/dining-sessions").session(role(UserRole.SERVICE_STAFF)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"tableId\":980001,\"packageId\":980001,\"soupId\":980001,\"adultCount\":1,\"childCount\":0}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.sessionStatus").value("ACTIVE"));
    }

    @Test void forceCloseKeepsRecordedPaymentAndReadyOrderHistoryButRemovesReadyWork() throws Exception {
        jdbc.update("UPDATE orders SET status='READY' WHERE id=980001");
        jdbc.update("INSERT INTO payments(id,session_id,amount,payment_method,payment_status,paid_at) VALUES (980001,980001,499,'CASH','PAID',CURRENT_TIMESTAMP)");
        mvc.perform(force("dining-sessions/980001/force-close").session(role(UserRole.MANAGER))).andExpect(status().isOk());
        entities.flush();
        assertThat(jdbc.queryForObject("SELECT amount FROM payments WHERE id=980001", java.math.BigDecimal.class)).isEqualByComparingTo("499");
        assertThat(jdbc.queryForObject("SELECT payment_status FROM payments WHERE id=980001", String.class)).isEqualTo("PAID");
        mvc.perform(get("/api/v1/orders/ready").session(role(UserRole.SERVICE_STAFF))).andExpect(jsonPath("$[?(@.sessionId == 980001)]").isEmpty());
        assertThat(jdbc.queryForObject("SELECT status FROM orders WHERE id=980001", String.class)).isEqualTo("READY");
    }

    @Test void forceDeleteHidesFromCatalogPreservesHistoryRejectsStaleCartAndCannotBeReactivated() throws Exception {
        Cookie cookie = customer();
        var manager = role(UserRole.MANAGER);
        mvc.perform(delete("/api/v1/menu-items/980001").session(manager)).andExpect(status().isBadRequest());
        mvc.perform(force("menu-items/980001/force-delete").session(manager)).andExpect(status().isNoContent());
        entities.flush();
        assertThat(jdbc.queryForObject("SELECT deleted_at IS NOT NULL AND available=false FROM menu_items WHERE id=980001", Boolean.class)).isTrue();
        assertThat(jdbc.queryForObject("SELECT item_name FROM order_items WHERE id=980001", String.class)).isEqualTo("Historical name");
        mvc.perform(get("/api/v1/menu-items/980001")).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/menu-items")).andExpect(jsonPath("$.content[?(@.id == 980001)]").isEmpty());
        mvc.perform(get("/api/v1/dining-sessions/980001/menu").cookie(cookie)).andExpect(jsonPath("$[?(@.id == 980001)]").isEmpty());
        mvc.perform(post("/api/v1/dining-sessions/980001/orders").cookie(cookie).header("Origin", "http://localhost:5173").contentType(MediaType.APPLICATION_JSON)
                .content("{\"items\":[{\"menuItemId\":980001,\"quantity\":1}]}"))
                .andExpect(status().isNotFound());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM orders WHERE session_id=980001", Integer.class)).isEqualTo(1);
        String input = "{\"categoryId\":980001,\"name\":\"History food\",\"available\":true,\"packageIds\":[980001]}";
        mvc.perform(put("/api/v1/menu-items/980001").session(manager).contentType(MediaType.APPLICATION_JSON).content(input)).andExpect(status().isNotFound());
        mvc.perform(post("/api/v1/menu-items").session(manager).contentType(MediaType.APPLICATION_JSON).content(input)).andExpect(status().isCreated());
        mvc.perform(force("menu-items/980001/force-delete").session(manager)).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/manager/operations").session(manager))
                .andExpect(jsonPath("$[0].action").value("FORCE_DELETE_MENU"))
                .andExpect(jsonPath("$[0].reason").value("test cleanup"))
                .andExpect(jsonPath("$[0].actorUsername").value("override-MANAGER"));
    }

    @ParameterizedTest @EnumSource(value = UserRole.class, names = {"SUPERVISOR", "SERVICE_STAFF", "KITCHEN_STAFF"})
    void deniesAllOtherRolesDespiteSpoofedManagerHeader(UserRole role) throws Exception {
        var session = role(role);
        mvc.perform(force("dining-sessions/980001/force-close").session(session).header("X-User-Role", "MANAGER")).andExpect(status().isForbidden());
        mvc.perform(force("menu-items/980001/force-delete").session(session).header("X-User-Role", "MANAGER")).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/manager/dining-sessions/active").session(session)).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/manager/operations").session(session)).andExpect(status().isForbidden());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM manager_operations", Integer.class)).isZero();
    }

    @Test void deniesAnonymousAndInvalidReasonsWithoutMutation() throws Exception {
        for (String path : new String[]{"dining-sessions/980001/force-close", "menu-items/980001/force-delete"}) {
            mvc.perform(force(path).header("X-User-Role", "MANAGER")).andExpect(status().isUnauthorized());
            for (String reason : new String[]{"", " ", "x".repeat(501)}) {
                mvc.perform(post("/api/v1/manager/" + path).session(role(UserRole.MANAGER)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"" + reason + "\"}")).andExpect(status().isBadRequest());
            }
        }
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM manager_operations", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT status FROM dining_sessions WHERE id=980001", String.class)).isEqualTo("ACTIVE");
    }

    @Test void managerListDoesNotExposeQrAndMissingResourcesDoNotWriteAudit() throws Exception {
        var manager = role(UserRole.MANAGER);
        mvc.perform(get("/api/v1/manager/dining-sessions/active").session(manager))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].sessionToken").doesNotExist());
        mvc.perform(force("dining-sessions/999999/force-close").session(manager)).andExpect(status().isNotFound());
        mvc.perform(force("menu-items/999999/force-delete").session(manager)).andExpect(status().isNotFound());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM manager_operations", Integer.class)).isZero();
    }
}
