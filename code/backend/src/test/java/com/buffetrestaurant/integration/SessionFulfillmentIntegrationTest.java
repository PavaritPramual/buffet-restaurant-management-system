package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.buffetrestaurant.domain.enums.UserRole;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.fulfillment.access-provider=session", "app.master-data.access-provider=session"})
@Transactional
class SessionFulfillmentIntegrationTest {
    @Autowired jakarta.persistence.EntityManager entityManager;
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder passwords;

    @BeforeEach
    void seed() {
        jdbc.update("INSERT INTO buffet_packages(id,name,price,active) VALUES (929001,'Role test',399,true)");
        jdbc.update("INSERT INTO soups(id,name,active) VALUES (929001,'Role soup',true)");
        jdbc.update("INSERT INTO restaurant_tables(id,table_number,capacity,status) VALUES (929001,'ROLE-IT',4,'OCCUPIED')");
        jdbc.update("INSERT INTO dining_sessions(id,table_id,package_id,soup_id,adult_count,child_count,session_token,status,package_price_at_open) VALUES (929001,929001,929001,929001,1,0,'role-test-qr','ACTIVE',399)");
        jdbc.update("INSERT INTO orders(id,session_id,table_number,status) VALUES (929001,929001,'ROLE-IT','RECEIVED')");
        int index = 0;
        String hash = passwords.encode("isolated-password");
        for (UserRole role : UserRole.values()) {
            long id = 929001L + index++;
            jdbc.update("INSERT INTO app_users(id,username,password_hash,role,active) VALUES (?,?,?,?,true)", id, "role-" + role, hash, role.name());
            jdbc.update("INSERT INTO user_profiles(user_id,display_name) VALUES (?,?)", id, role.name());
        }
    }

    private MockHttpSession login(UserRole role) throws Exception {
        return (MockHttpSession) mvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"role-" + role + "\",\"password\":\"isolated-password\"}"))
                .andExpect(status().isOk()).andReturn().getRequest().getSession(false);
    }

    private MockHttpServletRequestBuilder change(String target) {
        return patch("/api/v1/orders/929001/status").contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Role", "MANAGER").content("{\"status\":\"" + target + "\"}");
    }

    @ParameterizedTest
    @EnumSource(UserRole.class)
    void masterDataReadsAndMutationsUseActualSessionRole(UserRole role) throws Exception {
        MockHttpSession session = login(role);
        for (String path : new String[] {"tables", "buffet-packages", "soups"}) {
            mvc.perform(get("/api/v1/" + path).session(session).header("X-User-Role", "MANAGER"))
                    .andExpect(status().is(role == UserRole.KITCHEN_STAFF ? 403 : 200));
        }
        mvc.perform(put("/api/v1/tables/929001").session(session).header("X-User-Role", "MANAGER")
                .contentType(MediaType.APPLICATION_JSON).content("{\"tableNumber\":\"ROLE-IT\",\"capacity\":4}"))
                .andExpect(status().is(role == UserRole.MANAGER ? 200 : 403));
        for (String path : new String[] {"buffet-packages", "soups"}) {
            mvc.perform(patch("/api/v1/" + path + "/929001/active").session(session)
                    .header("X-User-Role", "MANAGER").contentType(MediaType.APPLICATION_JSON).content("{\"active\":false}"))
                    .andExpect(status().is(role == UserRole.MANAGER ? 200 : 403));
        }
    }

    @Test
    void anonymousCannotReadOrMutateMasterData() throws Exception {
        for (String path : new String[] {"tables", "buffet-packages", "soups"}) {
            mvc.perform(get("/api/v1/" + path).header("X-User-Role", "MANAGER"))
                    .andExpect(status().isUnauthorized());
            mvc.perform(delete("/api/v1/" + path + "/929001").header("X-User-Role", "MANAGER"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @ParameterizedTest
    @EnumSource(UserRole.class)
    void authenticatedRoleControlsBoardsAndTransitionsDespiteSpoofedHeader(UserRole role) throws Exception {
        MockHttpSession session = login(role);
        boolean kitchen = role == UserRole.KITCHEN_STAFF;
        boolean staff = role == UserRole.SERVICE_STAFF;
        mvc.perform(get("/api/v1/orders/incoming").session(session).header("X-User-Role", "KITCHEN_STAFF"))
                .andExpect(status().is(kitchen ? 200 : 403));
        mvc.perform(get("/api/v1/orders/ready").session(session).header("X-User-Role", "SERVICE_STAFF"))
                .andExpect(status().is(staff ? 200 : 403));
        mvc.perform(change("PREPARING").session(session)).andExpect(status().is(kitchen ? 200 : 403));
        entityManager.flush();
        entityManager.clear();
        assertThat(jdbc.queryForObject("SELECT status FROM orders WHERE id=929001", String.class))
                .isEqualTo(kitchen ? "PREPARING" : "RECEIVED");
        jdbc.update("UPDATE orders SET status='READY' WHERE id=929001");
        mvc.perform(change("SERVED").session(session)).andExpect(status().is(staff ? 200 : 403));
        entityManager.flush();
        assertThat(jdbc.queryForObject("SELECT status FROM orders WHERE id=929001", String.class))
                .isEqualTo(staff ? "SERVED" : "READY");
    }

    @Test
    void anonymousRoleHeaderCannotAuthenticate() throws Exception {
        mvc.perform(get("/api/v1/orders/incoming").header("X-User-Role", "KITCHEN_STAFF")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/orders/ready").header("X-User-Role", "SERVICE_STAFF")).andExpect(status().isUnauthorized());
        mvc.perform(change("PREPARING")).andExpect(status().isUnauthorized());
    }

    @Test
    void realLoginLifecycleAndLogout() throws Exception {
        MockHttpSession kitchen = login(UserRole.KITCHEN_STAFF);
        MockHttpSession staff = login(UserRole.SERVICE_STAFF);
        mvc.perform(change("READY").session(kitchen)).andExpect(status().isBadRequest());
        mvc.perform(change("PREPARING").session(kitchen)).andExpect(status().isOk());
        mvc.perform(change("READY").session(kitchen)).andExpect(status().isOk());
        mvc.perform(change("SERVED").session(staff)).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("SERVED"));
        mvc.perform(change("SERVED").session(staff)).andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/auth/logout").session(kitchen)).andExpect(status().isNoContent());
        assertThat(kitchen.isInvalid()).isTrue();
        mvc.perform(get("/api/v1/orders/incoming")).andExpect(status().isUnauthorized());
    }
}
