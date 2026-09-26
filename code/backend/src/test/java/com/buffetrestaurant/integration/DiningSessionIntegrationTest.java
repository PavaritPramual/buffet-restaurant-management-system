package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buffetrestaurant.domain.BuffetPackage;
import com.buffetrestaurant.domain.RestaurantTable;
import com.buffetrestaurant.domain.Soup;
import com.buffetrestaurant.domain.enums.PaymentStatus;
import com.buffetrestaurant.domain.enums.TableStatus;
import com.buffetrestaurant.integration.payment.PaymentStatusLookup;
import com.buffetrestaurant.repository.BuffetPackageRepository;
import com.buffetrestaurant.repository.DiningSessionRepository;
import com.buffetrestaurant.repository.RestaurantTableRepository;
import com.buffetrestaurant.repository.SoupRepository;
import com.buffetrestaurant.service.DiningSessionService;
import com.buffetrestaurant.dto.request.OpenDiningSessionRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class DiningSessionIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestaurantTableRepository tableRepository;

    @Autowired
    private BuffetPackageRepository packageRepository;

    @Autowired
    private SoupRepository soupRepository;

    @Autowired
    private DiningSessionRepository sessionRepository;

    @Autowired
    private DiningSessionService diningSessionService;

    @MockitoBean
    private PaymentStatusLookup paymentStatusLookup;

    private RestaurantTable table;
    private BuffetPackage buffetPackage;
    private Soup soup;

    @BeforeEach
    void prepareData() {
        sessionRepository.deleteAll();
        tableRepository.deleteAll();
        packageRepository.deleteAll();
        soupRepository.deleteAll();

        table = tableRepository.saveAndFlush(new RestaurantTable("T01", 4));
        buffetPackage = packageRepository.saveAndFlush(
                new BuffetPackage("Standard", new BigDecimal("299.00"), null));
        soup = soupRepository.saveAndFlush(new Soup("Tom Yum"));
    }

    @AfterEach
    void removeData() {
        sessionRepository.deleteAll();
        tableRepository.deleteAll();
        packageRepository.deleteAll();
        soupRepository.deleteAll();
    }

    @Test
    void opensAvailableTableAndReturnsSharedSessionContext() throws Exception {
        mockMvc.perform(post("/api/v1/dining-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(openRequest(2, 1)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.startsWith(
                        "/api/v1/dining-sessions/")))
                .andExpect(jsonPath("$.sessionId").isNumber())
                .andExpect(jsonPath("$.sessionToken").isString())
                .andExpect(jsonPath("$.sessionToken").value(org.hamcrest.Matchers.hasLength(43)))
                .andExpect(jsonPath("$.packageId").value(buffetPackage.getId()))
                .andExpect(jsonPath("$.tableId").value(table.getId()))
                .andExpect(jsonPath("$.tableNumber").value("T01"))
                .andExpect(jsonPath("$.sessionStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.adultCount").value(2))
                .andExpect(jsonPath("$.childCount").value(1));

        assertThat(tableRepository.findById(table.getId()).orElseThrow().getStatus())
                .isEqualTo(TableStatus.OCCUPIED);
        assertThat(sessionRepository.count()).isEqualTo(1);
    }

    @Test
    void rejectsOccupiedTableAndInvalidGuestCountsWithErrorResponse() throws Exception {
        table.occupy();
        tableRepository.saveAndFlush(table);
        mockMvc.perform(post("/api/v1/dining-sessions")
                        .contentType(MediaType.APPLICATION_JSON).content(openRequest(1, 0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/v1/dining-sessions"));

        table.makeAvailable();
        tableRepository.saveAndFlush(table);
        mockMvc.perform(post("/api/v1/dining-sessions")
                        .contentType(MediaType.APPLICATION_JSON).content(openRequest(4, 1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
        mockMvc.perform(post("/api/v1/dining-sessions")
                        .contentType(MediaType.APPLICATION_JSON).content(openRequest(0, 0)))
                .andExpect(status().isBadRequest());

        assertThat(sessionRepository.count()).isZero();
        assertThat(tableRepository.findById(table.getId()).orElseThrow().getStatus())
                .isEqualTo(TableStatus.AVAILABLE);
    }

    @Test
    void rejectsDisabledPackageOrSoup() throws Exception {
        buffetPackage.setActive(false);
        packageRepository.saveAndFlush(buffetPackage);
        mockMvc.perform(post("/api/v1/dining-sessions")
                        .contentType(MediaType.APPLICATION_JSON).content(openRequest(1, 0)))
                .andExpect(status().isBadRequest());

        buffetPackage.setActive(true);
        packageRepository.saveAndFlush(buffetPackage);
        soup.setActive(false);
        soupRepository.saveAndFlush(soup);
        mockMvc.perform(post("/api/v1/dining-sessions")
                        .contentType(MediaType.APPLICATION_JSON).content(openRequest(1, 0)))
                .andExpect(status().isBadRequest());
        assertThat(sessionRepository.count()).isZero();
    }

    @Test
    void tokenFindsOnlyActiveSessionAndPaidCloseReturnsTableToAvailable() throws Exception {
        String token = openThroughApiAndReadToken();
        long sessionId = sessionRepository.findAll().get(0).getId();

        mockMvc.perform(get("/api/v1/dining-sessions/token/" + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionStatus").value("ACTIVE"));
        mockMvc.perform(get("/api/v1/dining-sessions/token/not-a-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        when(paymentStatusLookup.findPaymentForSession(sessionId)).thenReturn(
                new PaymentStatusLookup.PaymentVerification(sessionId, PaymentStatus.PAID));
        mockMvc.perform(post("/api/v1/dining-sessions/" + sessionId + "/close"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.endTime").isString());

        assertThat(tableRepository.findById(table.getId()).orElseThrow().getStatus())
                .isEqualTo(TableStatus.AVAILABLE);
        mockMvc.perform(get("/api/v1/dining-sessions/token/" + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void closeRejectsUnpaidOrPaymentForDifferentSession() throws Exception {
        openThroughApiAndReadToken();
        long sessionId = sessionRepository.findAll().get(0).getId();

        when(paymentStatusLookup.findPaymentForSession(sessionId)).thenReturn(
                new PaymentStatusLookup.PaymentVerification(sessionId, PaymentStatus.PENDING));
        mockMvc.perform(post("/api/v1/dining-sessions/" + sessionId + "/close"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Dining session can only be closed after payment is PAID"));

        when(paymentStatusLookup.findPaymentForSession(sessionId)).thenReturn(
                new PaymentStatusLookup.PaymentVerification(sessionId + 1, PaymentStatus.PAID));
        mockMvc.perform(post("/api/v1/dining-sessions/" + sessionId + "/close"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Payment result belongs to a different dining session"));
        assertThat(tableRepository.findById(table.getId()).orElseThrow().getStatus())
                .isEqualTo(TableStatus.OCCUPIED);
    }

    @Test
    void concurrentOpenRequestsCreateOnlyOneActiveSession() throws Exception {
        OpenDiningSessionRequest request = new OpenDiningSessionRequest(
                table.getId(), buffetPackage.getId(), soup.getId(), 2, 0);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            List<CompletableFuture<Boolean>> results = java.util.stream.IntStream.range(0, 2)
                    .mapToObj(index -> CompletableFuture.supplyAsync(() -> {
                        try {
                            diningSessionService.openSession(request);
                            return true;
                        } catch (IllegalStateException exception) {
                            return false;
                        }
                    }, executor))
                    .toList();
            long successfulOpens = results.stream().map(CompletableFuture::join).filter(Boolean::booleanValue).count();
            assertThat(successfulOpens).isEqualTo(1);
            assertThat(sessionRepository.count()).isEqualTo(1);
            assertThat(tableRepository.findById(table.getId()).orElseThrow().getStatus())
                    .isEqualTo(TableStatus.OCCUPIED);
        } finally {
            executor.shutdownNow();
        }
    }

    private String openThroughApiAndReadToken() throws Exception {
        String response = mockMvc.perform(post("/api/v1/dining-sessions")
                        .contentType(MediaType.APPLICATION_JSON).content(openRequest(2, 0)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return new com.fasterxml.jackson.databind.ObjectMapper().readTree(response)
                .get("sessionToken").asText();
    }

    private String openRequest(int adults, int children) {
        return "{\"tableId\":" + table.getId()
                + ",\"packageId\":" + buffetPackage.getId()
                + ",\"soupId\":" + soup.getId()
                + ",\"adultCount\":" + adults
                + ",\"childCount\":" + children + "}";
    }
}
