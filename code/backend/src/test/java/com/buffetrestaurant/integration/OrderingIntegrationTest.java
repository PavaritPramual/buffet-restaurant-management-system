package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buffetrestaurant.domain.MenuCategory;
import com.buffetrestaurant.domain.MenuItem;
import com.buffetrestaurant.domain.enums.OrderStatus;
import com.buffetrestaurant.repository.CustomerOrderRepository;
import com.buffetrestaurant.repository.MenuCategoryRepository;
import com.buffetrestaurant.repository.MenuItemRepository;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderingIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private MenuCategoryRepository categoryRepository;
    @Autowired private MenuItemRepository itemRepository;
    @Autowired private CustomerOrderRepository orderRepository;

    private MenuCategory category;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        itemRepository.deleteAll();
        categoryRepository.deleteAll();
        category = categoryRepository.save(new MenuCategory("อาหารจานหลัก"));
    }

    @Test
    void placeOrder_whenValid_persistsReceivedOrderAndReturnsStatus() throws Exception {
        MenuItem item = itemRepository.save(new MenuItem(category, "ข้าวผัด", true, null, Set.of(1L)));

        mockMvc.perform(post("/api/v1/dining-sessions/1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"menuItemId\":" + item.getId() + ",\"quantity\":2}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sessionId").value(1))
                .andExpect(jsonPath("$.tableNumber").value("T01"))
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andExpect(jsonPath("$.items[0].name").value("ข้าวผัด"))
                .andExpect(jsonPath("$.items[0].quantity").value(2));

        assertThat(orderRepository.findAll()).singleElement()
                .extracting(order -> order.getStatus()).isEqualTo(OrderStatus.RECEIVED);
    }

    @Test
    void placeOrder_whenItemUnavailable_returns400() throws Exception {
        MenuItem item = itemRepository.save(new MenuItem(category, "ของหมด", false, null, Set.of(1L)));
        mockMvc.perform(post("/api/v1/dining-sessions/1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"menuItemId\":" + item.getId() + ",\"quantity\":1}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Menu item is unavailable: ของหมด"));
    }

    @Test
    void placeOrder_whenItemOutsidePackage_returns400() throws Exception {
        MenuItem item = itemRepository.save(new MenuItem(category, "พรีเมียม", true, null, Set.of(2L)));
        mockMvc.perform(post("/api/v1/dining-sessions/1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"menuItemId\":" + item.getId() + ",\"quantity\":1}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Menu item is not included in this package: พรีเมียม"));
    }

    @Test
    void placeOrder_whenSessionInactive_returns400() throws Exception {
        MenuItem item = itemRepository.save(new MenuItem(category, "ข้าวต้ม", true, null, Set.of(1L)));
        mockMvc.perform(post("/api/v1/dining-sessions/2/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"menuItemId\":" + item.getId() + ",\"quantity\":1}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Dining session is not active"));
    }

    @Test
    void placeOrder_whenQuantityIsZero_returns400WithoutSavingOrder() throws Exception {
        MenuItem item = itemRepository.save(new MenuItem(category, "ชาไทย", true, null, Set.of(1L)));
        mockMvc.perform(post("/api/v1/dining-sessions/1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"menuItemId\":" + item.getId() + ",\"quantity\":0}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("items[0].quantity: must be greater than 0"));
        assertThat(orderRepository.count()).isZero();
    }

    @Test
    void menuItems_supportPaginationAndSorting() throws Exception {
        itemRepository.save(new MenuItem(category, "ซุป", true, null, Set.of(1L)));
        itemRepository.save(new MenuItem(category, "กุ้ง", true, null, Set.of(1L)));

        mockMvc.perform(get("/api/v1/menu-items")
                        .param("page", "0").param("size", "1").param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("กุ้ง"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void menuForSession_onlyReturnsAvailableItemsInPackage() throws Exception {
        itemRepository.save(new MenuItem(category, "สั่งได้", true, null, Set.of(1L)));
        itemRepository.save(new MenuItem(category, "ปิดขาย", false, null, Set.of(1L)));
        itemRepository.save(new MenuItem(category, "คนละแพ็กเกจ", true, null, Set.of(2L)));

        mockMvc.perform(get("/api/v1/dining-sessions/1/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("สั่งได้"));
    }
}
