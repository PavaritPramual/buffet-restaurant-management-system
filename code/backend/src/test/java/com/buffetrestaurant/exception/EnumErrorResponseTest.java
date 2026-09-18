package com.buffetrestaurant.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.buffetrestaurant.domain.enums.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

class EnumErrorResponseTest {
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new ProbeController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void unsupportedEnumReturnsStandardBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/test/enum")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"received\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Malformed request or unsupported enum value"))
                .andExpect(jsonPath("$.path").value("/api/v1/test/enum"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @RestController
    @RequestMapping("/api/v1/test")
    static class ProbeController {
        @PostMapping("/enum")
        EnumProbeRequest probe(@RequestBody EnumProbeRequest request) {
            return request;
        }
    }

    record EnumProbeRequest(OrderStatus status) {
    }
}
