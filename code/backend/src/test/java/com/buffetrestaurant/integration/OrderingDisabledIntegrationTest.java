package com.buffetrestaurant.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.ordering.session-provider=disabled")
class OrderingDisabledIntegrationTest {
    @Autowired private MockMvc mockMvc;

    @Test
    void customerOrdering_whenSessionVerificationIsNotIntegrated_returns503() throws Exception {
        mockMvc.perform(get("/api/v1/dining-sessions/1/menu"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value(
                        "Customer ordering is unavailable until Dining Session verification is configured"
                ));
    }
}
