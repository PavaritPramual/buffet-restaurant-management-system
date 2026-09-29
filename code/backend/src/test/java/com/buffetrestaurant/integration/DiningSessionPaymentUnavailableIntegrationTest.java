package com.buffetrestaurant.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buffetrestaurant.domain.BuffetPackage;
import com.buffetrestaurant.domain.DiningSession;
import com.buffetrestaurant.domain.RestaurantTable;
import com.buffetrestaurant.domain.Soup;
import com.buffetrestaurant.repository.BuffetPackageRepository;
import com.buffetrestaurant.repository.DiningSessionRepository;
import com.buffetrestaurant.repository.RestaurantTableRepository;
import com.buffetrestaurant.repository.SoupRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class DiningSessionPaymentUnavailableIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private RestaurantTableRepository tableRepository;
    @Autowired private BuffetPackageRepository packageRepository;
    @Autowired private SoupRepository soupRepository;
    @Autowired private DiningSessionRepository sessionRepository;

    private Long sessionId;

    @BeforeEach
    void createActiveSession() {
        sessionRepository.deleteAll();
        tableRepository.deleteAll();
        packageRepository.deleteAll();
        soupRepository.deleteAll();

        RestaurantTable table = new RestaurantTable("NO-PAYMENT", 2);
        table.occupy();
        table = tableRepository.saveAndFlush(table);
        BuffetPackage buffetPackage = packageRepository.saveAndFlush(
                new BuffetPackage("Standard", new BigDecimal("299.00"), null));
        Soup soup = soupRepository.saveAndFlush(new Soup("Tom Yum"));
        DiningSession session = sessionRepository.saveAndFlush(new DiningSession(
                table, buffetPackage, soup, 1, 0, "unavailable-provider-token", LocalDateTime.now()));
        sessionId = session.getId();
    }

    @AfterEach
    void removeData() {
        sessionRepository.deleteAll();
        tableRepository.deleteAll();
        packageRepository.deleteAll();
        soupRepository.deleteAll();
    }

    @Test
    void closeReturnsServiceUnavailableWhenPaymentProviderIsNotConfigured() throws Exception {
        mockMvc.perform(post("/api/v1/dining-sessions/" + sessionId + "/close"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.error").value("Service Unavailable"))
                .andExpect(jsonPath("$.message").value("Payment status verification is not configured"));
    }
}
