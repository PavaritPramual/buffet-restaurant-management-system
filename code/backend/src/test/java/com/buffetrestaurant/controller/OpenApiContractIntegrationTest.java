package com.buffetrestaurant.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
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

    @Autowired
    private ObjectMapper objectMapper;

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

    @Test
    void documentsFulfillmentSuccessSchemasAndErrorResponseContracts() throws Exception {
        JsonNode api = readApiDocs();

        assertArrayResponseSchema(api, "/api/v1/orders/incoming", "get", "200", "OrderResponse");
        assertArrayResponseSchema(api, "/api/v1/orders/ready", "get", "200", "OrderResponse");
        assertResponseSchema(api, "/api/v1/orders/{id}/status", "patch", "200", "OrderResponse");

        for (ApiResponseRef response : List.of(
                new ApiResponseRef("/api/v1/orders/incoming", "get", "401"),
                new ApiResponseRef("/api/v1/orders/incoming", "get", "403"),
                new ApiResponseRef("/api/v1/orders/ready", "get", "401"),
                new ApiResponseRef("/api/v1/orders/ready", "get", "403"),
                new ApiResponseRef("/api/v1/orders/{id}/status", "patch", "400"),
                new ApiResponseRef("/api/v1/orders/{id}/status", "patch", "401"),
                new ApiResponseRef("/api/v1/orders/{id}/status", "patch", "403"),
                new ApiResponseRef("/api/v1/orders/{id}/status", "patch", "404"),
                new ApiResponseRef("/api/v1/orders/{id}/status", "patch", "409"),
                new ApiResponseRef("/api/v1/payments/sessions/{sessionId}", "get", "400"),
                new ApiResponseRef("/api/v1/payments/sessions/{sessionId}", "get", "401"),
                new ApiResponseRef("/api/v1/payments/sessions/{sessionId}", "get", "403"),
                new ApiResponseRef("/api/v1/payments/sessions/{sessionId}", "get", "404"),
                new ApiResponseRef("/api/v1/payments", "post", "400"),
                new ApiResponseRef("/api/v1/payments", "post", "401"),
                new ApiResponseRef("/api/v1/payments", "post", "403"),
                new ApiResponseRef("/api/v1/payments", "post", "404"),
                new ApiResponseRef("/api/v1/payments", "post", "409"),
                new ApiResponseRef("/api/v1/payments", "post", "503"),
                new ApiResponseRef("/api/v1/dining-sessions/qr-exchange", "post", "400"),
                new ApiResponseRef("/api/v1/dining-sessions/qr-exchange", "post", "403"),
                new ApiResponseRef("/api/v1/dining-sessions/customer-context", "get", "401"),
                new ApiResponseRef("/api/v1/dining-sessions/{sessionId}/bill-status", "get", "401"),
                new ApiResponseRef("/api/v1/dining-sessions/{sessionId}/bill-status", "get", "404"),
                new ApiResponseRef("/api/v1/dining-sessions/{sessionId}/bill-request", "post", "401"),
                new ApiResponseRef("/api/v1/dining-sessions/{sessionId}/bill-request", "post", "403"),
                new ApiResponseRef("/api/v1/dining-sessions/{sessionId}/bill-request", "post", "404"),
                new ApiResponseRef("/api/v1/dining-sessions/{id}/close", "post", "400"),
                new ApiResponseRef("/api/v1/dining-sessions/{id}/close", "post", "401"),
                new ApiResponseRef("/api/v1/dining-sessions/{id}/close", "post", "403"),
                new ApiResponseRef("/api/v1/dining-sessions/{id}/close", "post", "404"),
                new ApiResponseRef("/api/v1/billing/preview", "post", "400"),
                new ApiResponseRef("/api/v1/billing/preview", "post", "401"),
                new ApiResponseRef("/api/v1/billing/preview", "post", "403"),
                new ApiResponseRef("/api/v1/billing/preview", "post", "404"),
                new ApiResponseRef("/api/v1/billing/preview", "post", "503"),
                new ApiResponseRef("/api/v1/stock/items", "post", "400"),
                new ApiResponseRef("/api/v1/stock/items", "post", "401"),
                new ApiResponseRef("/api/v1/stock/items", "post", "403"),
                new ApiResponseRef("/api/v1/stock/items", "post", "409"),
                new ApiResponseRef("/api/v1/stock/items/{id}", "put", "400"),
                new ApiResponseRef("/api/v1/stock/items/{id}", "put", "401"),
                new ApiResponseRef("/api/v1/stock/items/{id}", "put", "403"),
                new ApiResponseRef("/api/v1/stock/items/{id}", "put", "404"),
                new ApiResponseRef("/api/v1/stock/items/{id}", "put", "409"),
                new ApiResponseRef("/api/v1/stock", "get", "401"),
                new ApiResponseRef("/api/v1/stock", "get", "403"),
                new ApiResponseRef("/api/v1/stock/{itemId}/in", "post", "400"),
                new ApiResponseRef("/api/v1/stock/{itemId}/in", "post", "401"),
                new ApiResponseRef("/api/v1/stock/{itemId}/in", "post", "403"),
                new ApiResponseRef("/api/v1/stock/{itemId}/in", "post", "404"),
                new ApiResponseRef("/api/v1/stock/{itemId}/adjustments", "post", "400"),
                new ApiResponseRef("/api/v1/stock/{itemId}/adjustments", "post", "401"),
                new ApiResponseRef("/api/v1/stock/{itemId}/adjustments", "post", "403"),
                new ApiResponseRef("/api/v1/stock/{itemId}/adjustments", "post", "404"),
                new ApiResponseRef("/api/v1/stock/transactions", "get", "401"),
                new ApiResponseRef("/api/v1/stock/transactions", "get", "403"),
                new ApiResponseRef("/api/v1/admin/users", "get", "401"),
                new ApiResponseRef("/api/v1/admin/users", "get", "403"),
                new ApiResponseRef("/api/v1/admin/users", "post", "400"),
                new ApiResponseRef("/api/v1/admin/users", "post", "401"),
                new ApiResponseRef("/api/v1/admin/users", "post", "403"),
                new ApiResponseRef("/api/v1/admin/users", "post", "409"),
                new ApiResponseRef("/api/v1/auth/login", "post", "400"),
                new ApiResponseRef("/api/v1/auth/login", "post", "401"))) {
            assertErrorResponseSchema(api, response);
        }

        JsonNode currentUserUnauthorized = api.path("paths").path("/api/v1/auth/me")
                .path("get").path("responses").path("401");
        Assertions.assertFalse(currentUserUnauthorized.has("content")
                        && !currentUserUnauthorized.path("content").isEmpty(),
                "GET /api/v1/auth/me 401 intentionally has no response body");
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));
    }

    @Test
    void documentsCreatedResponsesAndLocationHeaders() throws Exception {
        JsonNode api = readApiDocs();
        assertCreatedResponse(api, "/api/v1/menu-items", "MenuItemResponse", "/api/v1/menu-items/123");
        assertCreatedResponse(api, "/api/v1/dining-sessions/{sessionId}/orders", "OrderResponse",
                "/api/v1/dining-sessions/123/orders/456");
    }

    @Test
    void sharedErrorsPreserveFieldsWithoutOperationSpecificExamples() throws Exception {
        JsonNode api = readApiDocs();
        JsonNode schema = api.path("components").path("schemas").path("ErrorResponse");
        JsonNode properties = schema.path("properties");
        Set<String> fields = new HashSet<>();
        properties.fieldNames().forEachRemaining(fields::add);
        Assertions.assertEquals(Set.of("timestamp", "status", "error", "message", "path"), fields);
        Assertions.assertEquals("integer", properties.path("status").path("type").asText());
        for (String field : List.of("error", "message", "path")) {
            Assertions.assertEquals("string", properties.path(field).path("type").asText());
        }
        Assertions.assertEquals("date-time", properties.path("timestamp").path("format").asText());
        Assertions.assertFalse(schema.has("example"), "Shared errors must not imply a specific operation");
        for (String field : List.of("status", "error", "message", "path")) {
            Assertions.assertFalse(properties.path(field).has("example"),
                    "Shared " + field + " example would apply incorrectly to other statuses/operations");
        }
        // These operations previously rendered the Billing 400 example under unrelated errors.
        for (ApiResponseRef response : List.of(
                new ApiResponseRef("/api/v1/stock", "get", "401"),
                new ApiResponseRef("/api/v1/stock", "get", "403"),
                new ApiResponseRef("/api/v1/stock/items", "post", "409"),
                new ApiResponseRef("/api/v1/orders/incoming", "get", "401"),
                new ApiResponseRef("/api/v1/orders/incoming", "get", "403"),
                new ApiResponseRef("/api/v1/orders/{id}/status", "patch", "409"))) {
            assertErrorResponseSchema(api, response);
            JsonNode content = api.path("paths").path(response.path()).path(response.method())
                    .path("responses").path(response.code()).path("content");
            for (JsonNode mediaType : content) {
                Assertions.assertFalse(mediaType.has("example") || mediaType.has("examples"),
                        "Generic responses should use neutral schema samples: " + response);
            }
        }
    }

    private static void assertCreatedResponse(JsonNode api, String path, String schemaName, String locationExample) {
        JsonNode responses = api.path("paths").path(path).path("post").path("responses");
        Assertions.assertFalse(responses.has("200"), "Create must document runtime 201, not default 200: " + path);
        assertResponseSchema(api, path, "post", "201", schemaName);
        JsonNode location = responses.path("201").path("headers").path("Location").path("schema");
        Assertions.assertEquals("string", location.path("type").asText(), path + " Location type");
        Assertions.assertEquals("uri-reference", location.path("format").asText(), path + " Location format");
        Assertions.assertEquals(locationExample, location.path("example").asText(), path + " Location example");
    }

    private JsonNode readApiDocs() throws Exception {
        String apiDocs = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(apiDocs);
    }

    private static void assertResponseSchema(
            JsonNode api, String path, String method, String responseCode, String schemaName) {
        JsonNode response = api.path("paths").path(path).path(method).path("responses").path(responseCode);
        JsonNode content = response.path("content");
        JsonNode mediaType = content.has("application/json")
                ? content.path("application/json")
                : content.path("*/*");
        String actual = mediaType.path("schema").path("$ref").asText();
        Assertions.assertEquals("#/components/schemas/" + schemaName, actual,
                method.toUpperCase() + " " + path + " " + responseCode + " schema: " + response);
    }

    private static void assertArrayResponseSchema(
            JsonNode api, String path, String method, String responseCode, String itemSchemaName) {
        JsonNode schema = api.path("paths").path(path).path(method).path("responses").path(responseCode)
                .path("content").path("application/json").path("schema");
        Assertions.assertEquals("array", schema.path("type").asText(), method.toUpperCase() + " " + path
                + " response must be an array");
        Assertions.assertEquals("#/components/schemas/" + itemSchemaName, schema.path("items").path("$ref").asText(),
                method.toUpperCase() + " " + path + " array item schema");
    }

    private static void assertErrorResponseSchema(JsonNode api, ApiResponseRef response) {
        assertResponseSchema(api, response.path(), response.method(), response.code(), "ErrorResponse");
    }

    private record ApiResponseRef(String path, String method, String code) {
    }
}
