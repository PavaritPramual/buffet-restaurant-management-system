package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buffetrestaurant.repository.MenuCategoryRepository;
import com.buffetrestaurant.repository.MenuItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.menu.admin-access-provider=disabled")
class MenuAdminDisabledIntegrationTest {
    private static final String MESSAGE =
            "Menu administration is unavailable until staff authorization is configured";

    @Autowired private MockMvc mockMvc;
    @Autowired private MenuCategoryRepository categoryRepository;
    @Autowired private MenuItemRepository itemRepository;

    @Test
    void menuMutations_whenStaffAuthorizationIsNotIntegrated_return503WithoutChanges() throws Exception {
        long categoryCount = categoryRepository.count();
        long itemCount = itemRepository.count();

        assertUnavailable(post("/api/v1/menu-categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Unauthorized category\"}"));
        assertUnavailable(put("/api/v1/menu-categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Unauthorized update\"}"));
        assertUnavailable(delete("/api/v1/menu-categories/1"));
        assertUnavailable(post("/api/v1/menu-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"categoryId\":1,\"name\":\"Unauthorized item\","
                        + "\"available\":true,\"packageIds\":[1]}"));
        assertUnavailable(put("/api/v1/menu-items/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"categoryId\":1,\"name\":\"Unauthorized update\","
                        + "\"available\":true,\"packageIds\":[1]}"));
        assertUnavailable(delete("/api/v1/menu-items/1"));

        assertThat(categoryRepository.count()).isEqualTo(categoryCount);
        assertThat(itemRepository.count()).isEqualTo(itemCount);
    }

    private void assertUnavailable(
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request
    ) throws Exception {
        mockMvc.perform(request)
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value(MESSAGE));
    }
}
