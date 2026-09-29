package com.buffetrestaurant.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "app.dining-session.staff-access-provider=disabled")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DiningSessionStaffAccessDisabledIntegrationTest {
    @Autowired private MockMvc mockMvc;

    @Test
    void staffSessionEndpointsFailClosedUntilAuthorizationIsConfigured() throws Exception {
        mockMvc.perform(get("/api/v1/dining-sessions/active"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value(
                        "Staff table operations are unavailable until staff authorization is configured"));
        mockMvc.perform(post("/api/v1/dining-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tableId\":1,\"packageId\":1,\"soupId\":1,\"adultCount\":1,\"childCount\":0}"))
                .andExpect(status().isServiceUnavailable());
        mockMvc.perform(get("/api/v1/dining-sessions/1"))
                .andExpect(status().isServiceUnavailable());
        mockMvc.perform(post("/api/v1/dining-sessions/1/close"))
                .andExpect(status().isServiceUnavailable());
    }
}
