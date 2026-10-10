package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.buffetrestaurant.service.impl.CustomerSessionAccessService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties={"app.ordering.session-provider=database", "app.billing.context-provider=database", "app.master-data.access-provider=session"})
@AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class Step2CompletionIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired CustomerSessionAccessService access;
    @Autowired PasswordEncoder passwords;
    Cookie customer;
    @BeforeEach void seed() {
        jdbc.update("INSERT INTO buffet_packages(id,name,price,active) VALUES (940001,'Bill package',399,true)");
        jdbc.update("INSERT INTO soups(id,name,active) VALUES (940001,'Bill soup',true)");
        jdbc.update("INSERT INTO restaurant_tables(id,table_number,capacity,status) VALUES (940001,'BILL01',4,'OCCUPIED')");
        jdbc.update("INSERT INTO dining_sessions(id,table_id,package_id,soup_id,adult_count,child_count,package_price_at_open,session_token,status) VALUES (940001,940001,940001,940001,2,1,399,'bill-qr','ACTIVE')");
        customer = new Cookie(CustomerSessionAccessService.COOKIE_NAME, access.exchange("bill-qr").credential());
        int i = 0;
        for (String role : new String[]{"MANAGER","SUPERVISOR","SERVICE_STAFF","KITCHEN_STAFF"}) {
            jdbc.update("INSERT INTO app_users(id,username,password_hash,role,active) VALUES (?,?,?,?,true)", 940001L+i++, "complete-"+role, passwords.encode("test-password"), role);
            jdbc.update("INSERT INTO user_profiles(user_id,display_name) VALUES (?,?)", 940000L+i, role);
        }
    }
    MockHttpSession login(String role) throws Exception {
        return (MockHttpSession) mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"complete-"+role+"\",\"password\":\"test-password\"}"))
                .andExpect(status().isOk()).andReturn().getRequest().getSession(false);
    }
    @Test void requestIsIdempotentAndStopsOrdersForEveryDevice() throws Exception {
        var other = new Cookie(CustomerSessionAccessService.COOKIE_NAME, access.exchange(jdbc.queryForObject("SELECT session_token FROM dining_sessions WHERE id=940001",String.class)).credential());
        mvc.perform(get("/api/v1/dining-sessions/940001/bill-status").cookie(customer)).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("NOT_REQUESTED")).andExpect(jsonPath("$.bill.totalAmount").value(997.50));
        mvc.perform(post("/api/v1/dining-sessions/940001/bill-request").cookie(customer).header("Origin","http://localhost:5173"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("REQUESTED"))
                .andExpect(jsonPath("$.requestedAt").value(org.hamcrest.Matchers.endsWith("Z")));
        entityManager.flush();
        var first = jdbc.queryForObject("SELECT bill_requested_at FROM dining_sessions WHERE id=940001", java.time.OffsetDateTime.class);
        mvc.perform(post("/api/v1/dining-sessions/940001/bill-request").cookie(other).header("Origin","http://localhost:5173")).andExpect(status().isOk());
        entityManager.flush();
        assertThat(first).isNotNull();
        assertThat(jdbc.queryForObject("SELECT bill_requested_at FROM dining_sessions WHERE id=940001", java.time.OffsetDateTime.class)).isEqualTo(first);
        for (Cookie device : new Cookie[]{customer,other}) mvc.perform(post("/api/v1/dining-sessions/940001/orders").cookie(device).header("Origin","http://localhost:5173").contentType(MediaType.APPLICATION_JSON).content("{\"items\":[{\"menuItemId\":1,\"quantity\":1}]}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM orders WHERE session_id=940001",Integer.class)).isZero();
        mvc.perform(get("/api/v1/dining-sessions/940001/orders").cookie(other)).andExpect(status().isOk());
    }
    @Test void customerCredentialsOriginAndSessionAreEnforced() throws Exception {
        mvc.perform(get("/api/v1/dining-sessions/940001/bill-status")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/dining-sessions/940001/bill-status").cookie(new Cookie(CustomerSessionAccessService.COOKIE_NAME,"invalid"))).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/dining-sessions/940002/bill-status").cookie(customer)).andExpect(status().isNotFound());
        mvc.perform(post("/api/v1/dining-sessions/940001/bill-request").cookie(customer).header("Origin","https://invalid.example")).andExpect(status().isForbidden());
        jdbc.update("UPDATE customer_session_grants SET expires_at=created_at WHERE session_id=940001");
        // Clear managed grant cached by exchange before checking the expired credential.
        entityManager.clear();
        mvc.perform(get("/api/v1/dining-sessions/940001/bill-status").cookie(customer)).andExpect(status().isUnauthorized());
    }
    @Test void paymentRequiresRequestAndPaidSessionStillRejectsOrders() throws Exception {
        var staff = login("SERVICE_STAFF");
        String payment = "{\"sessionId\":940001,\"paymentMethod\":\"CASH\"}";
        mvc.perform(post("/api/v1/payments").session(staff).contentType(MediaType.APPLICATION_JSON).content(payment))
                .andExpect(status().isConflict());
        mvc.perform(get("/api/v1/dining-sessions/940001/bill-status").cookie(customer))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("NOT_REQUESTED"))
                .andExpect(jsonPath("$.bill.totalAmount").isNumber())
                .andExpect(jsonPath("$.dueAmount").isNumber())
                .andExpect(jsonPath("$.paidAmount").isNumber())
                .andExpect(jsonPath("$.bill.totalAmount").value(997.50))
                .andExpect(jsonPath("$.dueAmount").value(997.50)).andExpect(jsonPath("$.paidAmount").value(0));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM payments WHERE session_id=940001", Integer.class)).isZero();
        mvc.perform(post("/api/v1/dining-sessions/940001/bill-request").cookie(customer).header("Origin","http://localhost:5173"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("REQUESTED"))
                .andExpect(jsonPath("$.dueAmount").isNumber()).andExpect(jsonPath("$.paidAmount").isNumber())
                .andExpect(jsonPath("$.bill.totalAmount").value(997.50))
                .andExpect(jsonPath("$.dueAmount").value(997.50)).andExpect(jsonPath("$.paidAmount").value(0));
        mvc.perform(post("/api/v1/payments").session(staff).contentType(MediaType.APPLICATION_JSON).content(payment))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.paymentStatus").value("PAID"));
        mvc.perform(get("/api/v1/dining-sessions/940001/bill-status").cookie(customer))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.dueAmount").isNumber()).andExpect(jsonPath("$.paidAmount").isNumber())
                .andExpect(jsonPath("$.bill.totalAmount").value(997.50))
                .andExpect(jsonPath("$.dueAmount").value(0)).andExpect(jsonPath("$.paidAmount").value(997.50));
        mvc.perform(post("/api/v1/dining-sessions/940001/orders").cookie(customer).header("Origin","http://localhost:5173")
                .contentType(MediaType.APPLICATION_JSON).content("{\"items\":[{\"menuItemId\":1,\"quantity\":1}]}"))
                .andExpect(status().isConflict());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM orders WHERE session_id=940001", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT status FROM dining_sessions WHERE id=940001", String.class)).isEqualTo("ACTIVE");
    }
    @Autowired jakarta.persistence.EntityManager entityManager;
    @Test void openApiDefinesBillStateEnumAndSeparateAmounts() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.components.schemas.CustomerBillStatusResponse.properties.status.enum",
                        org.hamcrest.Matchers.containsInAnyOrder("NOT_REQUESTED", "REQUESTED", "PAID")))
                .andExpect(jsonPath("$.components.schemas.CustomerBillStatusResponse.properties.dueAmount.type").value("number"))
                .andExpect(jsonPath("$.components.schemas.CustomerBillStatusResponse.properties.paidAmount.type").value("number"));
    }
    @Test void priceSnapshotAndPaidStatusUseRecordedAmountWithoutClosing() throws Exception {
        jdbc.update("UPDATE buffet_packages SET price=999 WHERE id=940001"); entityManager.clear();
        mvc.perform(get("/api/v1/dining-sessions/940001/bill-status").cookie(customer)).andExpect(status().isOk()).andExpect(jsonPath("$.bill.totalAmount").value(997.50));
        jdbc.update("INSERT INTO payments(session_id,amount,payment_method,payment_status,paid_at) VALUES (940001,997.50,'CASH','PAID',CURRENT_TIMESTAMP)");
        mvc.perform(get("/api/v1/dining-sessions/940001/bill-status").cookie(customer)).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PAID")).andExpect(jsonPath("$.bill.totalAmount").value(997.50)).andExpect(jsonPath("$.paymentId").doesNotExist());
        assertThat(jdbc.queryForObject("SELECT status FROM dining_sessions WHERE id=940001",String.class)).isEqualTo("ACTIVE");
        jdbc.update("UPDATE dining_sessions SET status='COMPLETED' WHERE id=940001"); entityManager.clear();
        mvc.perform(get("/api/v1/dining-sessions/940001/bill-status").cookie(customer)).andExpect(status().isNotFound());
    }
    @Test void managerOnlyStockCatalogAndHistoryRules() throws Exception {
        String body="{\"sku\":\"S2-NEW\",\"name\":\"New item\",\"unit\":\"kg\",\"lowStockThreshold\":1.5}";
        mvc.perform(post("/api/v1/stock/items").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isUnauthorized());
        for(String role:new String[]{"SUPERVISOR","SERVICE_STAFF","KITCHEN_STAFF"}) mvc.perform(post("/api/v1/stock/items").session(login(role)).header("X-User-Role","MANAGER").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isForbidden());
        var manager=login("MANAGER");
        mvc.perform(post("/api/v1/stock/items").session(manager).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated()).andExpect(jsonPath("$.quantity").value(0));
        mvc.perform(post("/api/v1/stock/items").session(manager).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isConflict());
        long id=jdbc.queryForObject("SELECT id FROM stock_items WHERE sku='S2-NEW'",Long.class);
        mvc.perform(post("/api/v1/stock/"+id+"/in").session(manager).contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":2,\"reason\":\"opening stock\"}")).andExpect(status().isOk());
        mvc.perform(put("/api/v1/stock/items/"+id).session(manager).contentType(MediaType.APPLICATION_JSON).content(body.replace("kg","g"))).andExpect(status().isConflict());
        mvc.perform(put("/api/v1/stock/items/"+id).session(manager).contentType(MediaType.APPLICATION_JSON).content(body.replace("New item","Updated item"))).andExpect(status().isOk()).andExpect(jsonPath("$.quantity").value(2));
        mvc.perform(post("/api/v1/stock/items").session(manager).contentType(MediaType.APPLICATION_JSON).content(body.replace("1.5","-1"))).andExpect(status().isBadRequest());
    }
    @Test void activeAndHistoricalTablesCannotBeChangedOrDeleted() throws Exception {
        var manager=login("MANAGER");
        mvc.perform(put("/api/v1/tables/940001").session(manager).contentType(MediaType.APPLICATION_JSON).content("{\"tableNumber\":\"CHANGED\",\"capacity\":1}")).andExpect(status().isConflict());
        mvc.perform(patch("/api/v1/tables/940001/status").session(manager).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"AVAILABLE\"}")).andExpect(status().isConflict());
        mvc.perform(delete("/api/v1/tables/940001").session(manager))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "ไม่สามารถแก้ไขหรือลบโต๊ะได้ ขณะยังมีรอบใช้งานอยู่"));
        jdbc.update("UPDATE dining_sessions SET status='COMPLETED' WHERE id=940001"); jdbc.update("UPDATE restaurant_tables SET status='AVAILABLE' WHERE id=940001"); entityManager.clear();
        mvc.perform(delete("/api/v1/tables/940001").session(manager))
                .andExpect(status().isNoContent());
        assertThat(jdbc.queryForObject("SELECT archived FROM restaurant_tables WHERE id=940001", Boolean.class)).isTrue();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM dining_sessions WHERE id=940001", Integer.class)).isEqualTo(1);
    }
}
