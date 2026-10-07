package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buffetrestaurant.domain.CustomerOrder;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class OrderFulfillmentIntegrationTest {

    private static final String ROLE_HEADER = "X-User-Role";
    private static final String KITCHEN = "KITCHEN_STAFF";
    private static final String STAFF = "SERVICE_STAFF";
    private static final String MANAGER = "MANAGER";

    @Autowired private MockMvc mockMvc;
    @Autowired private MenuCategoryRepository categoryRepository;
    @Autowired private MenuItemRepository itemRepository;
    @Autowired private CustomerOrderRepository orderRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

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
        jdbcTemplate.update("INSERT INTO soups (id, name, active) VALUES (1, 'Tom Yum', true)");
        jdbcTemplate.update("INSERT INTO restaurant_tables (id, table_number, capacity, status) "
                + "VALUES (1, 'T01', 4, 'OCCUPIED')");
        jdbcTemplate.update("INSERT INTO dining_sessions "
                + "(id, table_id, package_id, soup_id, adult_count, child_count, "
                + "package_price_at_open, session_token, start_time, status) "
                + "VALUES (1, 1, 1, 1, 2, 0, 299, 'fulfillment-test-qr', CURRENT_TIMESTAMP, 'ACTIVE')");
        category = categoryRepository.save(new MenuCategory("อาหารจานหลัก"));
    }

    private CustomerOrder seedOrder(OrderStatus status) {
        MenuItem item = itemRepository.save(new MenuItem(category, "ข้าวผัด", true, null, Set.of(1L)));
        CustomerOrder order = new CustomerOrder(1L, "T01");
        order.addItem(item.getId(), item.getName(), 2);
        order.updateStatus(status);
        return orderRepository.save(order);
    }

    private MockHttpServletRequestBuilder patchStatus(Long orderId, String status, String role) {
        return patch("/api/v1/orders/" + orderId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .header(ROLE_HEADER, role)
                .content("{\"status\":\"" + status + "\"}");
    }

    @Test
    void fulfillmentLifecycle_whenAdvancedInOrder_movesThroughEveryStatus() throws Exception {
        CustomerOrder order = seedOrder(OrderStatus.RECEIVED);

        mockMvc.perform(get("/api/v1/orders/incoming").header(ROLE_HEADER, KITCHEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(order.getId()))
                .andExpect(jsonPath("$[0].status").value("RECEIVED"))
                .andExpect(jsonPath("$[0].createdAt").value(org.hamcrest.Matchers.endsWith("Z")));

        mockMvc.perform(patchStatus(order.getId(), "PREPARING", KITCHEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PREPARING"));

        mockMvc.perform(patchStatus(order.getId(), "READY", KITCHEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY"));

        mockMvc.perform(get("/api/v1/orders/ready").header(ROLE_HEADER, STAFF))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(order.getId()));

        mockMvc.perform(patchStatus(order.getId(), "SERVED", STAFF))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SERVED"));

        assertThat(orderRepository.findById(order.getId())).get()
                .extracting(CustomerOrder::getStatus).isEqualTo(OrderStatus.SERVED);
    }

    @Test
    void updateStatus_whenSkippingReceivedToReady_returns400AndLeavesOrderUnchanged() throws Exception {
        CustomerOrder order = seedOrder(OrderStatus.RECEIVED);

        mockMvc.perform(patchStatus(order.getId(), "READY", KITCHEN))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Cannot change order " + order.getId()
                                + " status from RECEIVED to READY; the only allowed next status is PREPARING"));

        assertThat(orderRepository.findById(order.getId())).get()
                .extracting(CustomerOrder::getStatus).isEqualTo(OrderStatus.RECEIVED);
    }

    @Test
    void updateStatus_whenReversingPreparingToReceived_returns400AndLeavesOrderUnchanged() throws Exception {
        CustomerOrder order = seedOrder(OrderStatus.PREPARING);

        mockMvc.perform(patchStatus(order.getId(), "RECEIVED", KITCHEN))
                .andExpect(status().isBadRequest());

        assertThat(orderRepository.findById(order.getId())).get()
                .extracting(CustomerOrder::getStatus).isEqualTo(OrderStatus.PREPARING);
    }

    @Test
    void updateStatus_whenOrderAlreadyServed_returns400() throws Exception {
        CustomerOrder order = seedOrder(OrderStatus.SERVED);

        mockMvc.perform(patchStatus(order.getId(), "SERVED", STAFF))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Order has already been served; no further status transitions are allowed"));
    }

    @Test
    void updateStatus_whenOrderDoesNotExist_returns404() throws Exception {
        mockMvc.perform(patchStatus(999999L, "PREPARING", KITCHEN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Order not found with id: 999999"));
    }

    @Test
    void getIncomingOrders_whenOrderIsReadyOrServed_excludesIt() throws Exception {
        seedOrder(OrderStatus.READY);
        seedOrder(OrderStatus.SERVED);

        mockMvc.perform(get("/api/v1/orders/incoming").header(ROLE_HEADER, KITCHEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // --- Authorization: 401/403 ---

    @Test
    void incoming_whenRoleHeaderMissing_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/orders/incoming"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.path").value("/api/v1/orders/incoming"));
    }

    @Test
    void incoming_whenRoleHeaderUnknown_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/orders/incoming").header(ROLE_HEADER, "not_a_real_role"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void incoming_whenCallerIsServiceStaff_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/orders/incoming").header(ROLE_HEADER, STAFF))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    void ready_whenRoleHeaderMissing_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/orders/ready"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void ready_whenCallerIsKitchenStaff_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/orders/ready").header(ROLE_HEADER, KITCHEN))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void updateStatus_whenRoleHeaderMissing_returns401AndLeavesOrderUnchanged() throws Exception {
        CustomerOrder order = seedOrder(OrderStatus.RECEIVED);

        mockMvc.perform(patch("/api/v1/orders/" + order.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PREPARING\"}"))
                .andExpect(status().isUnauthorized());

        assertThat(orderRepository.findById(order.getId())).get()
                .extracting(CustomerOrder::getStatus).isEqualTo(OrderStatus.RECEIVED);
    }

    @Test
    void updateStatus_whenServiceStaffTriesToStartPreparing_returns403AndLeavesOrderUnchanged() throws Exception {
        CustomerOrder order = seedOrder(OrderStatus.RECEIVED);

        mockMvc.perform(patchStatus(order.getId(), "PREPARING", STAFF))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Role SERVICE_STAFF is not permitted to perform this action"));

        assertThat(orderRepository.findById(order.getId())).get()
                .extracting(CustomerOrder::getStatus).isEqualTo(OrderStatus.RECEIVED);
    }

    @Test
    void updateStatus_whenKitchenStaffTriesToMarkServed_returns403AndLeavesOrderUnchanged() throws Exception {
        CustomerOrder order = seedOrder(OrderStatus.READY);

        mockMvc.perform(patchStatus(order.getId(), "SERVED", KITCHEN))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Role KITCHEN_STAFF is not permitted to perform this action"));

        assertThat(orderRepository.findById(order.getId())).get()
                .extracting(CustomerOrder::getStatus).isEqualTo(OrderStatus.READY);
    }

    @Test
    void managerCannotActOnEitherBoardAndOrderDoesNotChange() throws Exception {
        CustomerOrder order = seedOrder(OrderStatus.RECEIVED);
        mockMvc.perform(get("/api/v1/orders/incoming").header(ROLE_HEADER, MANAGER)).andExpect(status().isForbidden());
        mockMvc.perform(patchStatus(order.getId(), "PREPARING", MANAGER)).andExpect(status().isForbidden());
        mockMvc.perform(patchStatus(order.getId(), "READY", MANAGER)).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/orders/ready").header(ROLE_HEADER, MANAGER)).andExpect(status().isForbidden());
        mockMvc.perform(patchStatus(order.getId(), "SERVED", MANAGER)).andExpect(status().isForbidden());
        assertThat(orderRepository.findById(order.getId())).get()
                .extracting(CustomerOrder::getStatus).isEqualTo(OrderStatus.RECEIVED);
    }
}
