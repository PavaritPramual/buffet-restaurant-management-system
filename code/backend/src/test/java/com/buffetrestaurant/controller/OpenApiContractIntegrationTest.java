package com.buffetrestaurant.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiContractIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void documentsCookieFlowsAndNumericBillAmounts() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes.staffSessionCookie.type").value("apiKey"))
                .andExpect(jsonPath("$.components.securitySchemes.staffSessionCookie.in").value("cookie"))
                .andExpect(jsonPath("$.components.securitySchemes.staffSessionCookie.name").value("JSESSIONID"))
                .andExpect(jsonPath("$.components.securitySchemes.customerSessionCookie.in").value("cookie"))
                .andExpect(jsonPath("$.components.securitySchemes.customerSessionCookie.name").value("customer_session"))
                .andExpect(jsonPath("$.paths['/api/v1/dining-sessions/{sessionId}/bill-status'].get.security[0].customerSessionCookie")
                        .exists())
                .andExpect(jsonPath("$.paths['/api/v1/payments'].post.security[0].staffSessionCookie").exists())
                .andExpect(jsonPath("$.paths['/api/v1/dining-sessions/{id}/close'].post.security[0].staffSessionCookie")
                        .exists())
                .andExpect(jsonPath("$.components.schemas.CreatePaymentRequest.properties.amount").doesNotExist())
                .andExpect(jsonPath("$.components.schemas.CreatePaymentRequest.properties.paymentMethod.enum[0]")
                        .value("CASH"))
                .andExpect(jsonPath("$.components.schemas.ExchangeQrRequest.properties.token.writeOnly").value(true))
                .andExpect(jsonPath("$.components.schemas.CustomerBillStatusResponse.properties.dueAmount.type")
                        .value("number"))
                .andExpect(jsonPath("$.components.schemas.CustomerBillStatusResponse.properties.paidAmount.type")
                        .value("number"))
                .andExpect(jsonPath("$.components.schemas.BillSummary.properties.totalAmount.type").value("number"))
                .andExpect(jsonPath("$.components.schemas.ErrorResponse.properties.timestamp.format")
                        .value("date-time"));
    }
}
