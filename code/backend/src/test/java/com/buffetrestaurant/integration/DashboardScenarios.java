package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.buffetrestaurant.domain.*;
import com.buffetrestaurant.domain.enums.*;
import com.buffetrestaurant.dto.request.CreateUserRequest;
import com.buffetrestaurant.repository.*;
import com.buffetrestaurant.service.AuthService;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
abstract class DashboardScenarios {
    @Autowired MockMvc mvc;
    @Autowired AuthService auth;
    @Autowired PaymentRepository payments;
    @Autowired DiningSessionRepository sessions;
    @Autowired RestaurantTableRepository tables;
    @Autowired BuffetPackageRepository packages;
    @Autowired SoupRepository soups;
    @Autowired CustomerOrderRepository orders;
    @Autowired StockItemRepository stock;
    @Autowired UserAccountRepository users;
    @Autowired EntityManager em;
    private BuffetPackage buffet;
    private Soup soup;

    @BeforeEach void setup() {
        for (var role : UserRole.values()) auth.createUser(new CreateUserRequest(role.name(), "test12345",
                role.name(), null, role, "Demo", role.name(), null));
        buffet = packages.save(new BuffetPackage("Package", new BigDecimal("499.00"), "Test"));
        soup = soups.save(new Soup("Soup"));
    }

    @Test void onlyManagementCanReadAndCannotInheritPaymentOrKitchenPrivileges() throws Exception {
        mvc.perform(get("/api/v1/dashboard").header("X-User-Role", "MANAGER")).andExpect(status().isUnauthorized());
        for (var role : UserRole.values()) {
            var login = login(role);
            boolean management = role == UserRole.MANAGER || role == UserRole.SUPERVISOR;
            mvc.perform(get("/api/v1/dashboard").session(login).header("X-User-Role", "MANAGER"))
                    .andExpect(status().is(management ? 200 : 403));
            if (management) {
                mvc.perform(get("/api/v1/orders/incoming").session(login)).andExpect(status().isForbidden());
                mvc.perform(post("/api/v1/payments").session(login).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionId\":1,\"paymentMethod\":\"CASH\"}")).andExpect(status().isForbidden());
            }
        }
    }

    @Test void emptyPeriodHasZeroTotalsZeroFilledDatesAndAllPaymentMethods() throws Exception {
        mvc.perform(get("/api/v1/dashboard").session(login(UserRole.SUPERVISOR))
                        .param("startDate", "2026-10-01").param("endDate", "2026-10-03"))
                .andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.sales.revenue").value(0))
                .andExpect(jsonPath("$.sales.averageBill").value(0)).andExpect(jsonPath("$.sales.daily.length()").value(3))
                .andExpect(jsonPath("$.sales.byMethod.length()").value(3)).andExpect(jsonPath("$.sales.daily[1].revenue").value(0))
                .andExpect(jsonPath("$.operations.activeSessions").value(0)).andExpect(jsonPath("$.stock.lowItems").isEmpty());
    }

    @Test void paidAmountsUseBangkokBoundariesAndStoredAmountsEvenBeforeClose() throws Exception {
        var first = session(LocalDateTime.parse("2026-09-30T17:00:00"));
        var second = session(LocalDateTime.parse("2026-10-01T16:59:59"));
        var before = session(LocalDateTime.parse("2026-09-30T16:59:59"));
        var after = session(LocalDateTime.parse("2026-10-01T17:00:00"));
        paid(first, "10.01", PaymentMethod.CASH, "2026-09-30T17:00:00Z");
        paid(second, "20.02", PaymentMethod.QR, "2026-10-01T16:59:59Z");
        paid(before, "100.00", PaymentMethod.CARD, "2026-09-30T16:59:59Z");
        paid(after, "200.00", PaymentMethod.CARD, "2026-10-01T17:00:00Z");
        var pending = session(LocalDateTime.parse("2026-10-01T01:00:00"));
        pending.requestBill();
        payments.save(new Payment(pending.getId(), new BigDecimal("300.00"), PaymentMethod.CARD));
        buffet.update("New price", new BigDecimal("999.00"), "Changed after opening");
        em.flush();
        mvc.perform(get("/api/v1/dashboard").session(login(UserRole.MANAGER)).param("startDate", "2026-10-01").param("endDate", "2026-10-01"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.timeZone").value("Asia/Bangkok"))
                .andExpect(jsonPath("$.sales.revenue").value(30.03)).andExpect(jsonPath("$.sales.paidBills").value(2))
                .andExpect(jsonPath("$.sales.averageBill").value(15.02)).andExpect(jsonPath("$.sales.daily[0].revenue").value(30.03))
                .andExpect(jsonPath("$.sales.byMethod[0].revenue").value(10.01)).andExpect(jsonPath("$.sales.byMethod[1].revenue").value(20.02))
                .andExpect(jsonPath("$.sales.byMethod[2].revenue").value(0))
                .andExpect(jsonPath("$.visits.openedSessions").value(3)).andExpect(jsonPath("$.visits.adults").value(6))
                .andExpect(jsonPath("$.visits.children").value(3)).andExpect(jsonPath("$.operations.paidAwaitingClose").value(4))
                .andExpect(jsonPath("$.operations.unpaidBillRequests").value(1));
        assertThat(first.getStatus()).isEqualTo(DiningSessionStatus.ACTIVE);
        assertThat(first.getPackagePriceAtOpen()).isEqualByComparingTo("499.00");
    }

    @Test void currentOperationsExcludeArchivedStockAndTablesAndCompletedOrderQueues() throws Exception {
        var active = session(LocalDateTime.parse("2026-10-01T01:00:00"));
        var closed = session(LocalDateTime.parse("2026-09-01T01:00:00"));
        closed.complete(LocalDateTime.parse("2026-09-01T02:00:00"));
        var archivedTable = tables.save(new RestaurantTable("archived", 4)); archivedTable.archive();
        stock.save(new StockItem("LOW", "Rice", "kg", BigDecimal.ONE, BigDecimal.ONE, new BigDecimal("5")));
        stock.save(new StockItem("ZERO", "Milk", "l", BigDecimal.ZERO, BigDecimal.ONE));
        var inactive = stock.save(new StockItem("INACTIVE", "Hidden", "kg", BigDecimal.ZERO, BigDecimal.ONE)); inactive.changeActive(false);
        var archived = stock.save(new StockItem("ARCHIVED", "Archived", "kg", BigDecimal.ZERO, BigDecimal.ONE)); archived.archive();
        var category = new MenuCategory("Food");
        em.persist(category);
        var menu = new MenuItem(category, "Rice bowl", true, null, java.util.Set.of(buffet.getId()));
        em.persist(menu); em.flush();
        var received = new CustomerOrder(active.getId(), active.getRestaurantTable().getTableNumber());
        received.addItem(menu.getId(), "Rice bowl", 2); orders.save(received);
        var ready = new CustomerOrder(active.getId(), active.getRestaurantTable().getTableNumber());
        ready.addItem(menu.getId(), "Rice bowl", 3); ready.updateStatus(OrderStatus.READY); orders.save(ready);
        var old = new CustomerOrder(closed.getId(), closed.getRestaurantTable().getTableNumber());
        old.addItem(menu.getId(), "Rice bowl", 4); orders.save(old);
        em.flush();
        mvc.perform(get("/api/v1/dashboard").session(login(UserRole.SUPERVISOR)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.operations.availableTables").value(0))
                .andExpect(jsonPath("$.operations.occupiedTables").value(2)).andExpect(jsonPath("$.operations.activeGuests").value(3))
                .andExpect(jsonPath("$.operations.receivedOrders").value(1)).andExpect(jsonPath("$.operations.readyOrders").value(1))
                .andExpect(jsonPath("$.stock.activeItems").value(2)).andExpect(jsonPath("$.stock.lowStockItems").value(2))
                .andExpect(jsonPath("$.stock.outOfStockItems").value(1)).andExpect(jsonPath("$.stock.belowTargetItems").value(1))
                .andExpect(jsonPath("$.stock.lowItems[0].name").value("Milk"))
                .andExpect(jsonPath("$.stock.lowItems[1].shortfall").value(4))
                .andExpect(jsonPath("$.popularItems[0].servings").value(9));
    }

    @Test void malformedReversedLongAndFutureRangesAreRejectedAndDisabledSessionsRevoked() throws Exception {
        var manager = login(UserRole.MANAGER);
        for (var pair : new String[][]{{"bad", "2026-10-01"}, {"2026-10-03", "2026-10-01"},
                {"2020-01-01", "2026-01-01"}, {"2026-10-01", "+999999999-12-31"}}) {
            mvc.perform(get("/api/v1/dashboard").session(manager).param("startDate", pair[0]).param("endDate", pair[1]))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(get("/api/v1/dashboard").session(manager)).andExpect(status().isOk())
                .andExpect(jsonPath("$.startDate").value(LocalDate.now(java.time.ZoneId.of("Asia/Bangkok")).toString()));
        users.findByUsername("MANAGER").orElseThrow().changeActive(false);
        em.flush();
        mvc.perform(get("/api/v1/dashboard").session(manager)).andExpect(status().isUnauthorized());
    }

    private DiningSession session(LocalDateTime time) {
        var table = tables.save(new RestaurantTable("T-" + UUID.randomUUID().toString().substring(0, 8), 4));
        table.setStatus(TableStatus.OCCUPIED);
        return sessions.saveAndFlush(new DiningSession(table, buffet, soup, 2, 1, UUID.randomUUID().toString(), time));
    }
    private void paid(DiningSession session, String amount, PaymentMethod method, String at) {
        session.requestBill();
        var payment = new Payment(session.getId(), new BigDecimal(amount), method);
        payment.markPaid(OffsetDateTime.parse(at)); payments.saveAndFlush(payment);
    }
    private MockHttpSession login(UserRole role) throws Exception {
        return (MockHttpSession) mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + role.name() + "\",\"password\":\"test12345\"}"))
                .andExpect(status().isOk()).andReturn().getRequest().getSession(false);
    }
}
