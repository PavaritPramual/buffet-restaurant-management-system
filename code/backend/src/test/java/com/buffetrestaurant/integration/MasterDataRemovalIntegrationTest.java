package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.buffetrestaurant.service.CatalogService;
import com.buffetrestaurant.service.RestaurantTableService;
import com.buffetrestaurant.service.impl.CustomerSessionAccessService;
import com.buffetrestaurant.exception.AuthenticationRequiredException;
import jakarta.persistence.EntityManager;
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

@SpringBootTest(properties = {"app.master-data.access-provider=session", "app.billing.context-provider=database",
        "app.payment.status-provider=database", "app.dining-session.staff-access-provider=session",
        "app.ordering.session-provider=database"})
@AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class MasterDataRemovalIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder passwords;
    @Autowired CustomerSessionAccessService access;
    @Autowired EntityManager em;
    @Autowired CatalogService catalog;
    @Autowired RestaurantTableService tables;
    MockHttpSession manager, staff;

    @BeforeEach void seed() throws Exception {
        jdbc.update("INSERT INTO buffet_packages(id,name,price,active) VALUES (960001,'Standard',299,true)");
        jdbc.update("INSERT INTO soups(id,name,active) VALUES (960001,'Tom Yum',true)");
        jdbc.update("INSERT INTO restaurant_tables(id,table_number,capacity,status) VALUES (960001,'ARCH01',4,'OCCUPIED')");
        jdbc.update("INSERT INTO dining_sessions(id,table_id,package_id,soup_id,adult_count,child_count,package_price_at_open,session_token,status) VALUES (960001,960001,960001,960001,2,1,299,'archive-qr','ACTIVE')");
        for (int i = 0; i < 2; i++) {
            String role = i == 0 ? "MANAGER" : "SERVICE_STAFF";
            jdbc.update("INSERT INTO app_users(id,username,password_hash,role,active) VALUES (?,?,?,?,true)",960001L+i,"archive-"+role,passwords.encode("test-password"),role);
            jdbc.update("INSERT INTO user_profiles(user_id,display_name) VALUES (?,?)",960001L+i,role);
        }
        manager = login("MANAGER"); staff = login("SERVICE_STAFF");
    }

    private MockHttpSession login(String role) throws Exception {
        return (MockHttpSession) mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"archive-"+role+"\",\"password\":\"test-password\"}"))
                .andExpect(status().isOk()).andReturn().getRequest().getSession(false);
    }

    @Test void unusedRowsHardDeleteAndRepeatIsNotFound() throws Exception {
        jdbc.update("INSERT INTO restaurant_tables(id,table_number,capacity,status) VALUES (960002,'UNUSED',4,'AVAILABLE')");
        jdbc.update("INSERT INTO buffet_packages(id,name,price,active) VALUES (960002,'Unused',100,true)");
        jdbc.update("INSERT INTO soups(id,name,active) VALUES (960002,'Unused',true)");
        for (String resource : new String[]{"tables", "buffet-packages", "soups"}) {
            mvc.perform(delete("/api/v1/"+resource+"/960002").session(manager)).andExpect(status().isNoContent());
            mvc.perform(delete("/api/v1/"+resource+"/960002").session(manager)).andExpect(status().isNotFound());
        }
        assertThat(jdbc.queryForObject("SELECT archived FROM soups WHERE id=960001", Boolean.class)).isFalse();
    }

    @Test void removalListsAndRestoreRequireManagerEvenWithForgedHeader() throws Exception {
        for (String resource : new String[]{"tables", "buffet-packages", "soups"}) {
            mvc.perform(delete("/api/v1/"+resource+"/960001")).andExpect(status().isUnauthorized());
            mvc.perform(delete("/api/v1/"+resource+"/960001").session(staff).header("X-User-Role","MANAGER")).andExpect(status().isForbidden());
            mvc.perform(get("/api/v1/"+resource+"/archived").session(staff)).andExpect(status().isForbidden());
            mvc.perform(post("/api/v1/"+resource+"/960001/restore").session(staff)).andExpect(status().isForbidden());
        }
        assertThat(jdbc.queryForObject("SELECT archived FROM buffet_packages WHERE id=960001", Boolean.class)).isFalse();
    }

    @Test void directServiceCallsCannotBypassAuthorization() {
        org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
        assertThatThrownBy(() -> tables.deleteTable(960001L)).isInstanceOf(AuthenticationRequiredException.class);
        assertThatThrownBy(() -> tables.getArchivedTables()).isInstanceOf(AuthenticationRequiredException.class);
        assertThatThrownBy(() -> tables.restoreTable(960001L)).isInstanceOf(AuthenticationRequiredException.class);
        assertThatThrownBy(() -> catalog.removePackage(960001L)).isInstanceOf(AuthenticationRequiredException.class);
        assertThatThrownBy(() -> catalog.getArchivedSoups()).isInstanceOf(AuthenticationRequiredException.class);
        assertThatThrownBy(() -> catalog.restoreSoup(960001L)).isInstanceOf(AuthenticationRequiredException.class);
    }

    @Test void archiveKeepsExistingBillingAndCloseThenRestoreRetriesPreserveState() throws Exception {
        Cookie customer = new Cookie(CustomerSessionAccessService.COOKIE_NAME,access.exchange("archive-qr").credential());
        mvc.perform(delete("/api/v1/tables/960001").session(manager)).andExpect(status().isConflict());
        for (String resource : new String[]{"buffet-packages", "soups"}) {
            mvc.perform(delete("/api/v1/"+resource+"/960001").session(manager)).andExpect(status().isNoContent());
            mvc.perform(delete("/api/v1/"+resource+"/960001").session(manager)).andExpect(status().isNoContent());
            mvc.perform(get("/api/v1/"+resource).session(manager)).andExpect(jsonPath("$.length()").value(0));
            mvc.perform(get("/api/v1/"+resource+"/archived").session(manager)).andExpect(jsonPath("$[0].archived").value(true));
            mvc.perform(patch("/api/v1/"+resource+"/960001/active").session(manager).contentType(MediaType.APPLICATION_JSON).content("{\"active\":true}")).andExpect(status().isConflict());
        }
        mvc.perform(get("/api/v1/dining-sessions/960001").session(staff)).andExpect(jsonPath("$.packageName").value("Standard")).andExpect(jsonPath("$.soupName").value("Tom Yum"));
        mvc.perform(post("/api/v1/dining-sessions/960001/bill-request").cookie(customer).header("Origin","http://localhost:5173"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.bill.totalAmount").value(747.50));
        mvc.perform(post("/api/v1/payments").session(staff).contentType(MediaType.APPLICATION_JSON).content("{\"sessionId\":960001,\"paymentMethod\":\"CASH\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.amount").value(747.50));
        mvc.perform(post("/api/v1/dining-sessions/960001/close").session(staff)).andExpect(status().isOk()).andExpect(jsonPath("$.sessionStatus").value("COMPLETED"));
        mvc.perform(delete("/api/v1/tables/960001").session(manager)).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/tables").session(manager)).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/v1/dining-sessions/960001").session(staff)).andExpect(jsonPath("$.packageName").value("Standard"));
        mvc.perform(post("/api/v1/tables/960001/restore").session(manager)).andExpect(jsonPath("$.status").value("AVAILABLE"));
        for (String resource : new String[]{"buffet-packages", "soups"}) {
            mvc.perform(post("/api/v1/"+resource+"/960001/restore").session(manager)).andExpect(jsonPath("$.active").value(false)).andExpect(jsonPath("$.archived").value(false));
            mvc.perform(patch("/api/v1/"+resource+"/960001/active").session(manager).contentType(MediaType.APPLICATION_JSON).content("{\"active\":true}")).andExpect(status().isOk());
            mvc.perform(post("/api/v1/"+resource+"/960001/restore").session(manager)).andExpect(jsonPath("$.active").value(true));
        }
        mvc.perform(post("/api/v1/dining-sessions").session(staff).contentType(MediaType.APPLICATION_JSON)
                .content("{\"tableId\":960001,\"packageId\":960001,\"soupId\":960001,\"adultCount\":1,\"childCount\":0}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/v1/tables/960001/restore").session(manager)).andExpect(jsonPath("$.status").value("OCCUPIED"));
        assertThat(jdbc.queryForObject("SELECT package_price_at_open FROM dining_sessions WHERE id=960001",java.math.BigDecimal.class)).isEqualByComparingTo("299");
    }

    @Test void packageWithOnlyMenuMembershipArchivesWithoutDeletingMembership() throws Exception {
        jdbc.update("INSERT INTO buffet_packages(id,name,price,active) VALUES (960002,'Linked',100,true)");
        jdbc.update("INSERT INTO menu_categories(id,name) VALUES (960001,'Meat')");
        jdbc.update("INSERT INTO menu_items(id,category_id,name,available) VALUES (960001,960001,'Pork',true)");
        jdbc.update("INSERT INTO package_menu_items(package_id,menu_item_id) VALUES (960002,960001)");
        mvc.perform(delete("/api/v1/buffet-packages/960002").session(manager)).andExpect(status().isNoContent());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM package_menu_items WHERE package_id=960002",Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT archived FROM buffet_packages WHERE id=960002",Boolean.class)).isTrue();
    }

    @Test void newQrCustomerCanLoadArchivedSessionPackageOrderAndPayButCannotSelectItForNewSession() throws Exception {
        jdbc.update("INSERT INTO restaurant_tables(id,table_number,capacity,status) VALUES (960002,'ARCH02',4,'AVAILABLE')");
        jdbc.update("INSERT INTO restaurant_tables(id,table_number,capacity,status) VALUES (960003,'ARCH03',4,'AVAILABLE')");
        jdbc.update("INSERT INTO menu_categories(id,name) VALUES (960001,'Meat')");
        jdbc.update("INSERT INTO menu_items(id,category_id,name,available) VALUES (960001,960001,'Pork',true)");
        jdbc.update("INSERT INTO package_menu_items(package_id,menu_item_id) VALUES (960001,960001)");
        var json = new com.fasterxml.jackson.databind.ObjectMapper();
        var opened = json.readTree(mvc.perform(post("/api/v1/dining-sessions").session(staff)
                .contentType(MediaType.APPLICATION_JSON).content("{\"tableId\":960002,\"packageId\":960001,\"soupId\":960001,\"adultCount\":2,\"childCount\":1}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        long sessionId = opened.get("sessionId").asLong();
        String path = "/api/v1/dining-sessions/" + sessionId;
        mvc.perform(delete("/api/v1/buffet-packages/960001").session(manager)).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/buffet-packages").session(manager)).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/v1/buffet-packages/960001").session(manager)).andExpect(status().isConflict());
        mvc.perform(post("/api/v1/dining-sessions").session(staff)
                .contentType(MediaType.APPLICATION_JSON).content("{\"tableId\":960003,\"packageId\":960001,\"soupId\":960001,\"adultCount\":1,\"childCount\":0}"))
                .andExpect(status().isBadRequest());
        var exchange = mvc.perform(post("/api/v1/dining-sessions/qr-exchange")
                .header("Origin", "http://localhost:5173").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(java.util.Map.of("token", opened.get("sessionToken").asText()))))
                .andExpect(status().isOk()).andReturn().getResponse();
        Cookie customer = exchange.getCookie(CustomerSessionAccessService.COOKIE_NAME);
        assertThat(customer).isNotNull();
        mvc.perform(get(path + "/package")).andExpect(status().isUnauthorized());
        mvc.perform(get(path + "/package").cookie(new Cookie(CustomerSessionAccessService.COOKIE_NAME, "invalid")))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/dining-sessions/960001/package").cookie(customer)).andExpect(status().isNotFound());
        mvc.perform(get(path + "/package").cookie(customer)).andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.id").value(960001)).andExpect(jsonPath("$.name").value("Standard"))
                .andExpect(jsonPath("$.active").value(false)).andExpect(jsonPath("$.archived").value(true));
        mvc.perform(get(path + "/menu").cookie(customer)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Pork"));
        mvc.perform(get(path + "/bill-status").cookie(customer)).andExpect(status().isOk())
                .andExpect(jsonPath("$.bill.totalAmount").value(747.50));
        mvc.perform(post(path + "/orders").cookie(customer).header("Origin", "http://localhost:5173")
                .contentType(MediaType.APPLICATION_JSON).content("{\"items\":[{\"menuItemId\":960001,\"quantity\":1}]}"))
                .andExpect(status().isCreated());
        mvc.perform(post(path + "/bill-request").cookie(customer).header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.bill.totalAmount").value(747.50));
        mvc.perform(post("/api/v1/payments").session(staff).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(java.util.Map.of("sessionId", sessionId, "paymentMethod", "CASH"))))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.amount").value(747.50));
        mvc.perform(post(path + "/close").session(staff)).andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionStatus").value("COMPLETED"));
        mvc.perform(get(path + "/package").cookie(customer)).andExpect(status().isUnauthorized());
        assertThat(jdbc.queryForObject("SELECT package_price_at_open FROM dining_sessions WHERE id=?", java.math.BigDecimal.class, sessionId))
                .isEqualByComparingTo("299");
    }
}
