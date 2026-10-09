package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buffetrestaurant.domain.MenuCategory;
import com.buffetrestaurant.domain.MenuItem;
import com.buffetrestaurant.domain.CustomerOrder;
import com.buffetrestaurant.domain.enums.OrderStatus;
import com.buffetrestaurant.repository.CustomerOrderRepository;
import com.buffetrestaurant.repository.MenuCategoryRepository;
import com.buffetrestaurant.repository.MenuItemRepository;
import java.util.Set;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import jakarta.servlet.http.Cookie;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "app.ordering.session-provider=database")
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class OrderingIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private MenuCategoryRepository categoryRepository;
    @Autowired private MenuItemRepository itemRepository;
    @Autowired private CustomerOrderRepository orderRepository;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private EntityManager entityManager;

    private MenuCategory category;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        itemRepository.deleteAll();
        categoryRepository.deleteAll();
        jdbcTemplate.update("DELETE FROM customer_session_grants");
        jdbcTemplate.update("DELETE FROM dining_sessions");
        jdbcTemplate.update("DELETE FROM restaurant_tables");
        jdbcTemplate.update("DELETE FROM soups");
        jdbcTemplate.update("DELETE FROM buffet_packages");
        jdbcTemplate.update(
                "INSERT INTO buffet_packages (id, name, price, description, active) VALUES (?, ?, ?, ?, ?)",
                1L, "Standard", 299, null, true);
        jdbcTemplate.update(
                "INSERT INTO buffet_packages (id, name, price, description, active) VALUES (?, ?, ?, ?, ?)",
                2L, "Premium", 499, null, true);
        jdbcTemplate.update("INSERT INTO soups (id, name, active) VALUES (?, ?, ?)", 1L, "Tom Yum", true);
        jdbcTemplate.update("INSERT INTO restaurant_tables (id, table_number, capacity, status) VALUES (?, ?, ?, ?)",
                1L, "T01", 4, "OCCUPIED");
        jdbcTemplate.update("INSERT INTO restaurant_tables (id, table_number, capacity, status) VALUES (?, ?, ?, ?)",
                2L, "T02", 4, "OCCUPIED");
        jdbcTemplate.update("INSERT INTO dining_sessions "
                        + "(id, table_id, package_id, soup_id, adult_count, child_count, "
                        + "package_price_at_open, session_token, start_time, status) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)",
                1L, 1L, 1L, 1L, 2, 0, 299, "fixture-active-token", "ACTIVE");
        jdbcTemplate.update("INSERT INTO dining_sessions "
                        + "(id, table_id, package_id, soup_id, adult_count, child_count, "
                        + "package_price_at_open, session_token, start_time, status) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)",
                2L, 2L, 1L, 1L, 2, 0, 299, "fixture-second-token", "ACTIVE");
        addGrant(1L, "fixture-active-credential");
        addGrant(2L, "fixture-second-credential");
        category = categoryRepository.save(new MenuCategory("อาหารจานหลัก"));
    }

    private void addGrant(long sessionId, String credential) {
        try {
            String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(credential.getBytes(StandardCharsets.UTF_8)));
            OffsetDateTime now = OffsetDateTime.now();
            jdbcTemplate.update("INSERT INTO customer_session_grants "
                    + "(session_id, token_hash, created_at, expires_at) VALUES (?, ?, ?, ?)",
                    sessionId, hash, now, now.plusHours(8));
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private Cookie customerCookie(String credential) {
        return new Cookie("customer_session", credential);
    }

    @Test
    void placeOrder_whenValid_persistsReceivedOrderAndReturnsStatus() throws Exception {
        MenuItem item = itemRepository.save(new MenuItem(category, "ข้าวผัด", true, null, Set.of(1L)));

        mockMvc.perform(post("/api/v1/dining-sessions/1/orders")
                        .cookie(customerCookie("fixture-active-credential"))
                        .header("Origin", "http://localhost:5173")
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
    void getOrder_whenOrderBelongsToSession_returnsOrder() throws Exception {
        MenuItem item = itemRepository.save(new MenuItem(category, "ข้าวผัด", true, null, Set.of(1L)));
        CustomerOrder order = new CustomerOrder(1L, "T01");
        order.addItem(item.getId(), item.getName(), 1);
        order = orderRepository.save(order);

        mockMvc.perform(get("/api/v1/dining-sessions/1/orders/" + order.getId())
                        .cookie(customerCookie("fixture-active-credential")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(order.getId()))
                .andExpect(jsonPath("$.sessionId").value(1));
    }

    @Test
    void getOrder_whenOrderBelongsToAnotherSession_returns404WithoutDisclosure() throws Exception {
        MenuItem item = itemRepository.save(new MenuItem(category, "ข้าวผัด", true, null, Set.of(1L)));
        CustomerOrder order = new CustomerOrder(1L, "T01");
        order.addItem(item.getId(), item.getName(), 1);
        order = orderRepository.save(order);

        mockMvc.perform(get("/api/v1/dining-sessions/2/orders/" + order.getId())
                        .cookie(customerCookie("fixture-second-credential")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Order not found with id: " + order.getId()));
    }

    @Test
    void menuOrdersAndOrderDetails_requireTokenMatchingSessionAndActiveStatus() throws Exception {
        MenuItem item = itemRepository.save(new MenuItem(category, "ข้าวผัด", true, null, Set.of(1L)));
        String basePath = "/api/v1/dining-sessions/1";

        mockMvc.perform(get(basePath + "/menu")).andExpect(status().isUnauthorized());
        mockMvc.perform(get(basePath + "/orders").cookie(customerCookie("wrong-token")))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post(basePath + "/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"menuItemId\":" + item.getId() + ",\"quantity\":1}]}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get(basePath + "/orders/999").cookie(customerCookie("wrong-token")))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/dining-sessions/2/menu")
                        .cookie(customerCookie("fixture-active-credential")))
                .andExpect(status().isNotFound());

        jdbcTemplate.update("UPDATE dining_sessions SET status = 'COMPLETED' WHERE id = 1");
        entityManager.clear();
        mockMvc.perform(get(basePath + "/menu").cookie(customerCookie("fixture-active-credential")))
                .andExpect(status().isNotFound());
        mockMvc.perform(post(basePath + "/orders").cookie(customerCookie("fixture-active-credential"))
                        .header("Origin", "http://localhost:5173")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"menuItemId\":" + item.getId() + ",\"quantity\":1}]}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void placeOrder_whenItemUnavailable_returns400() throws Exception {
        MenuItem item = itemRepository.save(new MenuItem(category, "ของหมด", false, null, Set.of(1L)));
        mockMvc.perform(post("/api/v1/dining-sessions/1/orders")
                        .cookie(customerCookie("fixture-active-credential"))
                        .header("Origin", "http://localhost:5173")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"menuItemId\":" + item.getId() + ",\"quantity\":1}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Menu item is unavailable: ของหมด"));
        assertThat(orderRepository.count()).isZero();
    }

    @Test
    void placeOrder_whenItemOutsidePackage_returns400() throws Exception {
        MenuItem item = itemRepository.save(new MenuItem(category, "พรีเมียม", true, null, Set.of(2L)));
        mockMvc.perform(post("/api/v1/dining-sessions/1/orders")
                        .cookie(customerCookie("fixture-active-credential"))
                        .header("Origin", "http://localhost:5173")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"menuItemId\":" + item.getId() + ",\"quantity\":1}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Menu item is not included in this package: พรีเมียม"));
        assertThat(orderRepository.count()).isZero();
    }

    @Test
    void placeOrder_whenSessionInactive_returns404() throws Exception {
        MenuItem item = itemRepository.save(new MenuItem(category, "ข้าวต้ม", true, null, Set.of(1L)));
        jdbcTemplate.update("UPDATE dining_sessions SET status = 'COMPLETED' WHERE id = 1");
        entityManager.clear();
        mockMvc.perform(post("/api/v1/dining-sessions/1/orders")
                        .cookie(customerCookie("fixture-active-credential"))
                        .header("Origin", "http://localhost:5173")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"menuItemId\":" + item.getId() + ",\"quantity\":1}]}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void placeOrder_whenQuantityIsZero_returns400WithoutSavingOrder() throws Exception {
        MenuItem item = itemRepository.save(new MenuItem(category, "ชาไทย", true, null, Set.of(1L)));
        mockMvc.perform(post("/api/v1/dining-sessions/1/orders")
                        .cookie(customerCookie("fixture-active-credential"))
                        .header("Origin", "http://localhost:5173")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"menuItemId\":" + item.getId() + ",\"quantity\":0}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("items[0].quantity: must be greater than 0"));
        assertThat(orderRepository.count()).isZero();
    }

    @Test
    void placeOrder_whenQuantityIsNegative_returns400WithoutSavingOrder() throws Exception {
        MenuItem item = itemRepository.save(new MenuItem(category, "ชาไทย", true, null, Set.of(1L)));
        mockMvc.perform(post("/api/v1/dining-sessions/1/orders")
                        .cookie(customerCookie("fixture-active-credential"))
                        .header("Origin", "http://localhost:5173")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"menuItemId\":" + item.getId() + ",\"quantity\":-1}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("items[0].quantity: must be greater than 0"));
        assertThat(orderRepository.count()).isZero();
    }

    @Test
    void placeOrder_whenDuplicateMenuItemIds_returns400WithoutSavingOrder() throws Exception {
        MenuItem item = itemRepository.save(new MenuItem(category, "ชาไทย", true, null, Set.of(1L)));
        mockMvc.perform(post("/api/v1/dining-sessions/1/orders")
                        .cookie(customerCookie("fixture-active-credential"))
                        .header("Origin", "http://localhost:5173")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"menuItemId\":" + item.getId() + ",\"quantity\":1},"
                                + "{\"menuItemId\":" + item.getId() + ",\"quantity\":2}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Duplicate menu item in order: " + item.getId()));
        assertThat(orderRepository.count()).isZero();
    }

    @Test
    void deleteMenuItem_whenItemHasOrderHistory_returns400AndKeepsHistory() throws Exception {
        MenuItem item = itemRepository.save(new MenuItem(category, "ข้าวผัด", true, null, Set.of(1L)));
        CustomerOrder order = new CustomerOrder(1L, "T01");
        order.addItem(item.getId(), item.getName(), 1);
        orderRepository.saveAndFlush(order);

        mockMvc.perform(delete("/api/v1/menu-items/" + item.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "เมนูนี้มีประวัติการสั่งซื้อ จึงลบถาวรไม่ได้"));

        assertThat(itemRepository.existsById(item.getId())).isTrue();
        assertThat(orderRepository.count()).isOne();
    }

    @Test
    void deleteMenuItem_whenItemHasNoOrderHistory_returns204() throws Exception {
        MenuItem item = itemRepository.save(new MenuItem(category, "ข้าวผัด", true, null, Set.of(1L)));

        mockMvc.perform(delete("/api/v1/menu-items/" + item.getId()))
                .andExpect(status().isNoContent());

        assertThat(itemRepository.existsById(item.getId())).isFalse();
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
    void createMenuItem_whenPackageDoesNotExist_returns400WithoutSavingItem() throws Exception {
        mockMvc.perform(post("/api/v1/menu-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\":" + category.getId()
                                + ",\"name\":\"เมนูแพ็กเกจผิด\",\"available\":true,\"packageIds\":[999]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Buffet packages not found: [999]"));

        assertThat(itemRepository.count()).isZero();
    }

    @Test
    void menuForSession_onlyReturnsAvailableItemsInPackage() throws Exception {
        itemRepository.save(new MenuItem(category, "สั่งได้", true, null, Set.of(1L)));
        itemRepository.save(new MenuItem(category, "ปิดขาย", false, null, Set.of(1L)));
        itemRepository.save(new MenuItem(category, "คนละแพ็กเกจ", true, null, Set.of(2L)));

        mockMvc.perform(get("/api/v1/dining-sessions/1/menu")
                        .cookie(customerCookie("fixture-active-credential")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("สั่งได้"));
    }
}
