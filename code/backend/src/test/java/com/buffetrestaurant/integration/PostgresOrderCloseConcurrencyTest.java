package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

import com.buffetrestaurant.domain.enums.PaymentStatus;
import com.buffetrestaurant.dto.request.OrderItemRequest;
import com.buffetrestaurant.dto.request.PlaceOrderRequest;
import com.buffetrestaurant.exception.ResourceNotFoundException;
import com.buffetrestaurant.exception.UnauthorizedException;
import com.buffetrestaurant.integration.payment.PaymentStatusLookup;
import com.buffetrestaurant.service.CustomerOrderingService;
import com.buffetrestaurant.service.DiningSessionService;
import com.buffetrestaurant.service.impl.CustomerSessionAccessService;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(properties = {"app.ordering.session-provider=database", "app.billing.context-provider=database"})
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "DINING_TEST_PG_URL", matches = ".+")
@EnabledIfEnvironmentVariable(named = "ALLOW_DESTRUCTIVE_DB_TESTS", matches = "true")
class PostgresOrderCloseConcurrencyTest {
    private static final long SESSION_ID = 990001L;
    private static final long MENU_ID = 990001L;

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        String url = DisposablePostgresDatabase.requireReady("DINING_TEST");
        registry.add("spring.datasource.url", () -> url);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.datasource.username", () -> System.getenv().getOrDefault("DINING_TEST_PG_USER", "postgres"));
        registry.add("spring.datasource.password", () -> System.getenv().getOrDefault("DINING_TEST_PG_PASSWORD", ""));
        registry.add("spring.flyway.locations", () ->
                "classpath:db/migration/common,classpath:db/migration/postgresql");
    }

    @Autowired private JdbcTemplate jdbc;
    @Autowired private TransactionTemplate transactions;
    @Autowired private CustomerSessionAccessService customerAccess;
    @Autowired private CustomerOrderingService ordering;
    @Autowired private DiningSessionService sessions;
    @MockitoBean private PaymentStatusLookup paymentStatusLookup;
    @MockitoBean private com.buffetrestaurant.service.PaymentAccessProvider paymentAccess;
    @Autowired private com.buffetrestaurant.service.PaymentService payments;

    @Autowired private com.buffetrestaurant.service.CustomerBillingService billing;
    @Autowired private com.buffetrestaurant.service.ManagerOperationsService manager;
    @Autowired private com.buffetrestaurant.repository.MenuItemRepository menus;
    @MockitoBean private com.buffetrestaurant.service.UserContextProvider users;
    private String credential;

    @BeforeEach
    void setUp() {
        clean();
        jdbc.update("INSERT INTO buffet_packages(id,name,price,active) VALUES (990001,'Race Package',299,true)");
        jdbc.update("INSERT INTO soups(id,name,active) VALUES (990001,'Race Soup',true)");
        jdbc.update("INSERT INTO restaurant_tables(id,table_number,capacity,status) "
                + "VALUES (990001,'RACE01',4,'OCCUPIED')");
        jdbc.update("INSERT INTO dining_sessions(id,table_id,package_id,soup_id,adult_count,child_count,"
                + "package_price_at_open,session_token,status) "
                + "VALUES (990001,990001,990001,990001,2,0,299,'race-qr','ACTIVE')");
        jdbc.update("INSERT INTO menu_categories(id,name) VALUES (990001,'Race Category')");
        jdbc.update("INSERT INTO menu_items(id,category_id,name,available) "
                + "VALUES (990001,990001,'Race Item',true)");
        jdbc.update("INSERT INTO package_menu_items(package_id,menu_item_id) VALUES (990001,990001)");
        credential = customerAccess.exchange("race-qr").credential();
        when(users.requireCurrentRequestRole(com.buffetrestaurant.domain.enums.UserRole.MANAGER))
                .thenReturn(new com.buffetrestaurant.dto.response.UserContext(990001L, "race-manager", "Race Manager",
                        com.buffetrestaurant.domain.enums.UserRole.MANAGER));
        when(paymentStatusLookup.findPaymentForSession(SESSION_ID)).thenReturn(
                new PaymentStatusLookup.PaymentVerification(SESSION_ID, PaymentStatus.PAID));
    }

    @AfterEach
    void tearDown() {
        clean();
    }

    private void clean() {
        String url = DisposablePostgresDatabase.requireUrl("DINING_TEST", System.getenv());
        jdbc.execute((ConnectionCallback<Void>) connection -> {
            DisposablePostgresDatabase.verifyDatabase(connection, url);
            return null;
        });
        jdbc.update("DELETE FROM order_items WHERE order_id IN (SELECT id FROM orders WHERE session_id = ?)", SESSION_ID);
        jdbc.update("DELETE FROM orders WHERE session_id = ?", SESSION_ID);
        jdbc.update("DELETE FROM customer_session_grants WHERE session_id = ?", SESSION_ID);
        jdbc.update("DELETE FROM payments WHERE session_id = ?", SESSION_ID);
        jdbc.update("DELETE FROM manager_operations WHERE resource_id = ?", SESSION_ID);
        jdbc.update("DELETE FROM package_menu_items WHERE package_id = 990001");
        jdbc.update("DELETE FROM menu_items WHERE id = ?", MENU_ID);
        jdbc.update("DELETE FROM menu_categories WHERE id = 990001");
        jdbc.update("DELETE FROM dining_sessions WHERE id = ?", SESSION_ID);
        jdbc.update("DELETE FROM restaurant_tables WHERE id = 990001");
        jdbc.update("DELETE FROM soups WHERE id = 990001");
        jdbc.update("DELETE FROM buffet_packages WHERE id = 990001");
    }

    @Test
    void orderHoldingSessionLockCommitsBeforeClose() throws Exception {
        CountDownLatch orderLocked = new CountDownLatch(1);
        CountDownLatch releaseOrder = new CountDownLatch(1);
        CountDownLatch closeStarted = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            CompletableFuture<Void> order = CompletableFuture.runAsync(() -> transactions.executeWithoutResult(status -> {
                customerAccess.requireSession(SESSION_ID, credential, true);
                orderLocked.countDown();
                await(releaseOrder);
                ordering.placeOrder(SESSION_ID, credential,
                        new PlaceOrderRequest(List.of(new OrderItemRequest(MENU_ID, 1))));
            }), executor);
            assertThat(orderLocked.await(5, TimeUnit.SECONDS)).isTrue();
            CompletableFuture<Void> close = CompletableFuture.runAsync(() -> {
                closeStarted.countDown();
                sessions.closeSession(SESSION_ID);
            }, executor);
            assertThat(closeStarted.await(5, TimeUnit.SECONDS)).isTrue();
            waitForSessionLockWait();
            releaseOrder.countDown();
            order.join();
            close.join();
            assertThat(jdbc.queryForObject("SELECT count(*) FROM orders WHERE session_id = ?",
                    Integer.class, SESSION_ID)).isOne();
            assertThat(jdbc.queryForObject("SELECT status FROM dining_sessions WHERE id = ?",
                    String.class, SESSION_ID)).isEqualTo("COMPLETED");
        } finally {
            releaseOrder.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void closeHoldingSessionLockRejectsLaterOrder() throws Exception {
        CountDownLatch closeLocked = new CountDownLatch(1);
        CountDownLatch releaseClose = new CountDownLatch(1);
        CountDownLatch orderStarted = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            CompletableFuture<Void> close = CompletableFuture.runAsync(() -> transactions.executeWithoutResult(status -> {
                sessions.closeSession(SESSION_ID);
                closeLocked.countDown();
                await(releaseClose);
            }), executor);
            assertThat(closeLocked.await(5, TimeUnit.SECONDS)).isTrue();
            CompletableFuture<Boolean> order = CompletableFuture.supplyAsync(() -> {
                orderStarted.countDown();
                try {
                    ordering.placeOrder(SESSION_ID, credential,
                            new PlaceOrderRequest(List.of(new OrderItemRequest(MENU_ID, 1))));
                    return true;
                } catch (ResourceNotFoundException | com.buffetrestaurant.exception.UnauthorizedException exception) {
                    return false;
                }
            }, executor);
            assertThat(orderStarted.await(5, TimeUnit.SECONDS)).isTrue();
            waitForSessionLockWait();
            releaseClose.countDown();
            close.join();
            assertThat(order.join()).isFalse();
            assertThat(jdbc.queryForObject("SELECT count(*) FROM orders WHERE session_id = ?",
                    Integer.class, SESSION_ID)).isZero();
        } finally {
            releaseClose.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void orderAfterCommittedCloseIsUnauthorizedAndPersistsNothing() {
        sessions.closeSession(SESSION_ID);
        assertThatExceptionOfType(UnauthorizedException.class)
                .isThrownBy(() -> ordering.placeOrder(SESSION_ID, credential,
                        new PlaceOrderRequest(List.of(new OrderItemRequest(MENU_ID, 1)))));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM orders WHERE session_id = ?",
                Integer.class, SESSION_ID)).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM customer_session_grants WHERE session_id = ?",
                Integer.class, SESSION_ID)).isZero();
        assertThat(jdbc.queryForObject("SELECT status FROM dining_sessions WHERE id = ?",
                String.class, SESSION_ID)).isEqualTo("COMPLETED");
    }

    @Test
    void orderHoldingSessionLockCommitsBeforeBillRequest() throws Exception {
        CountDownLatch orderLocked = new CountDownLatch(1);
        CountDownLatch releaseOrder = new CountDownLatch(1);
        CountDownLatch closeStarted = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            CompletableFuture<Void> order = CompletableFuture.runAsync(() -> transactions.executeWithoutResult(status -> {
                customerAccess.requireSession(SESSION_ID, credential, true);
                orderLocked.countDown();
                await(releaseOrder);
                ordering.placeOrder(SESSION_ID, credential,
                        new PlaceOrderRequest(List.of(new OrderItemRequest(MENU_ID, 1))));
            }), executor);
            assertThat(orderLocked.await(5, TimeUnit.SECONDS)).isTrue();
            CompletableFuture<Void> close = CompletableFuture.runAsync(() -> {
                closeStarted.countDown();
                billing.request(SESSION_ID, credential);
            }, executor);
            assertThat(closeStarted.await(5, TimeUnit.SECONDS)).isTrue();
            waitForSessionLockWait();
            releaseOrder.countDown();
            order.join();
            close.join();
            assertThat(jdbc.queryForObject("SELECT count(*) FROM orders WHERE session_id = ?",
                    Integer.class, SESSION_ID)).isOne();
            assertThat(jdbc.queryForObject("SELECT status FROM dining_sessions WHERE id = ?",
                    String.class, SESSION_ID)).isEqualTo("ACTIVE");
        } finally {
            releaseOrder.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void billRequestHoldingSessionLockRejectsLaterOrder() throws Exception {
        CountDownLatch closeLocked = new CountDownLatch(1);
        CountDownLatch releaseClose = new CountDownLatch(1);
        CountDownLatch orderStarted = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            CompletableFuture<Void> close = CompletableFuture.runAsync(() -> transactions.executeWithoutResult(status -> {
                billing.request(SESSION_ID, credential);
                closeLocked.countDown();
                await(releaseClose);
            }), executor);
            assertThat(closeLocked.await(5, TimeUnit.SECONDS)).isTrue();
            CompletableFuture<Boolean> order = CompletableFuture.supplyAsync(() -> {
                orderStarted.countDown();
                try {
                    ordering.placeOrder(SESSION_ID, credential,
                            new PlaceOrderRequest(List.of(new OrderItemRequest(MENU_ID, 1))));
                    return true;
                } catch (com.buffetrestaurant.exception.DuplicateResourceException exception) {
                    return false;
                }
            }, executor);
            assertThat(orderStarted.await(5, TimeUnit.SECONDS)).isTrue();
            waitForSessionLockWait();
            releaseClose.countDown();
            close.join();
            assertThat(order.join()).isFalse();
            assertThat(jdbc.queryForObject("SELECT count(*) FROM orders WHERE session_id = ?",
                    Integer.class, SESSION_ID)).isZero();
        } finally {
            releaseClose.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void orderBillRequestAndPaymentSerializeOnTheSessionLock() throws Exception {
        CountDownLatch locked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(3);
        try {
            var order = CompletableFuture.runAsync(() -> transactions.executeWithoutResult(status -> {
                customerAccess.requireSession(SESSION_ID, credential, true);
                locked.countDown();
                await(release);
                ordering.placeOrder(SESSION_ID, credential,
                        new PlaceOrderRequest(List.of(new OrderItemRequest(MENU_ID, 1))));
            }), executor);
            assertThat(locked.await(5, TimeUnit.SECONDS)).isTrue();
            var request = CompletableFuture.runAsync(() -> billing.request(SESSION_ID, credential), executor);
            var payment = CompletableFuture.supplyAsync(() -> {
                try { pay(); return true; }
                catch (com.buffetrestaurant.exception.DuplicateResourceException exception) { return false; }
            }, executor);
            waitForSessionLockWait(2);
            assertThat(jdbc.queryForObject("SELECT count(*) FROM payments WHERE session_id=?", Integer.class, SESSION_ID)).isZero();
            release.countDown();
            order.join();
            request.join();
            // PostgreSQL may grant either waiter first. A payment ahead of Request Bill must fail.
            if (!payment.join()) pay();
            assertThat(jdbc.queryForObject("SELECT count(*) FROM orders WHERE session_id=?", Integer.class, SESSION_ID)).isOne();
            assertThat(jdbc.queryForObject("SELECT count(*) FROM payments WHERE session_id=?", Integer.class, SESSION_ID)).isOne();
            assertThatExceptionOfType(com.buffetrestaurant.exception.DuplicateResourceException.class)
                    .isThrownBy(() -> ordering.placeOrder(SESSION_ID, credential,
                            new PlaceOrderRequest(List.of(new OrderItemRequest(MENU_ID, 1)))));
        } finally { release.countDown(); executor.shutdownNow(); }
    }

    @Test
    void paymentWaitsForBillRequestToCommit() throws Exception {
        CountDownLatch locked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            var request = CompletableFuture.runAsync(() -> transactions.executeWithoutResult(status -> {
                billing.request(SESSION_ID, credential);
                locked.countDown();
                await(release);
            }), executor);
            assertThat(locked.await(5, TimeUnit.SECONDS)).isTrue();
            var payment = CompletableFuture.runAsync(this::pay, executor);
            waitForSessionLockWait();
            assertThat(jdbc.queryForObject("SELECT count(*) FROM payments WHERE session_id=?", Integer.class, SESSION_ID)).isZero();
            release.countDown();
            request.join(); payment.join();
            assertThat(jdbc.queryForObject("SELECT payment_status FROM payments WHERE session_id=?", String.class, SESSION_ID)).isEqualTo("PAID");
        } finally { release.countDown(); executor.shutdownNow(); }
    }

    @Test
    void paymentRechecksCompletedStateAfterWaitingForClose() throws Exception {
        billing.request(SESSION_ID, credential);
        CountDownLatch locked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            var close = CompletableFuture.runAsync(() -> transactions.executeWithoutResult(status -> {
                sessions.closeSession(SESSION_ID);
                locked.countDown(); await(release);
            }), executor);
            assertThat(locked.await(5, TimeUnit.SECONDS)).isTrue();
            var payment = CompletableFuture.supplyAsync(() -> {
                try { pay(); return true; }
                catch (IllegalStateException exception) { return false; }
            }, executor);
            waitForSessionLockWait();
            release.countDown(); close.join();
            assertThat(payment.join()).isFalse();
            assertThat(jdbc.queryForObject("SELECT count(*) FROM payments WHERE session_id=?", Integer.class, SESSION_ID)).isZero();
        } finally { release.countDown(); executor.shutdownNow(); }
    }

    @Test
    void orderCommitsBeforeWaitingForceCloseAndHistoryIsPreserved() throws Exception {
        CountDownLatch locked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            var order = CompletableFuture.runAsync(() -> transactions.executeWithoutResult(status -> {
                ordering.placeOrder(SESSION_ID, credential,
                        new PlaceOrderRequest(List.of(new OrderItemRequest(MENU_ID, 2))));
                locked.countDown(); await(release);
            }), executor);
            assertThat(locked.await(5, TimeUnit.SECONDS)).isTrue();
            var close = CompletableFuture.runAsync(() -> manager.forceClose(SESSION_ID, "Reset test table"), executor);
            waitForSessionLockWait();
            release.countDown(); order.join(); close.join();
            assertThat(jdbc.queryForObject("SELECT status FROM dining_sessions WHERE id=?", String.class, SESSION_ID)).isEqualTo("CANCELLED");
            assertThat(jdbc.queryForObject("SELECT count(*) FROM orders WHERE session_id=?", Integer.class, SESSION_ID)).isOne();
            assertThat(jdbc.queryForObject("SELECT count(*) FROM payments WHERE session_id=?", Integer.class, SESSION_ID)).isZero();
        } finally { release.countDown(); executor.shutdownNow(); }
    }

    @Test
    void forceCloseHoldingLockRejectsWaitingOrderAndRevokesGrant() throws Exception {
        CountDownLatch locked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            var close = CompletableFuture.runAsync(() -> transactions.executeWithoutResult(status -> {
                manager.forceClose(SESSION_ID, "Reset test table");
                locked.countDown(); await(release);
            }), executor);
            assertThat(locked.await(5, TimeUnit.SECONDS)).isTrue();
            var order = CompletableFuture.supplyAsync(() -> {
                try {
                    ordering.placeOrder(SESSION_ID, credential,
                            new PlaceOrderRequest(List.of(new OrderItemRequest(MENU_ID, 1))));
                    return true;
                } catch (UnauthorizedException | ResourceNotFoundException exception) { return false; }
            }, executor);
            waitForSessionLockWait();
            release.countDown(); close.join();
            assertThat(order.join()).isFalse();
            assertThat(jdbc.queryForObject("SELECT count(*) FROM orders WHERE session_id=?", Integer.class, SESSION_ID)).isZero();
            assertThat(jdbc.queryForObject("SELECT count(*) FROM customer_session_grants WHERE session_id=?", Integer.class, SESSION_ID)).isZero();
        } finally { release.countDown(); executor.shutdownNow(); }
    }

    @Test
    void forceDeleteHoldingMenuLockRejectsStaleCartAfterCommit() throws Exception {
        CountDownLatch locked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            var deletion = CompletableFuture.runAsync(() -> transactions.executeWithoutResult(status -> {
                manager.forceDeleteMenu(MENU_ID, "Remove test food");
                locked.countDown(); await(release);
            }), executor);
            assertThat(locked.await(5, TimeUnit.SECONDS)).isTrue();
            var order = CompletableFuture.supplyAsync(() -> {
                try {
                    ordering.placeOrder(SESSION_ID, credential,
                            new PlaceOrderRequest(List.of(new OrderItemRequest(MENU_ID, 1))));
                    return true;
                } catch (ResourceNotFoundException exception) { return false; }
            }, executor);
            waitForLockWait("menu_items", 1);
            release.countDown(); deletion.join();
            assertThat(order.join()).isFalse();
            assertThat(jdbc.queryForObject("SELECT count(*) FROM orders WHERE session_id=?", Integer.class, SESSION_ID)).isZero();
        } finally { release.countDown(); executor.shutdownNow(); }
    }

    @Test
    void orderHoldingMenuLockCommitsBeforeForceDeleteWithoutLosingItemHistory() throws Exception {
        CountDownLatch locked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            var order = CompletableFuture.runAsync(() -> transactions.executeWithoutResult(status -> {
                menus.findByIdForUpdate(MENU_ID).orElseThrow();
                locked.countDown(); await(release);
                ordering.placeOrder(SESSION_ID, credential,
                        new PlaceOrderRequest(List.of(new OrderItemRequest(MENU_ID, 2))));
            }), executor);
            assertThat(locked.await(5, TimeUnit.SECONDS)).isTrue();
            var deletion = CompletableFuture.runAsync(() -> manager.forceDeleteMenu(MENU_ID, "Remove test food"), executor);
            waitForLockWait("menu_items", 1);
            release.countDown(); order.join(); deletion.join();
            assertThat(jdbc.queryForObject("SELECT item_name FROM order_items WHERE menu_item_id=?", String.class, MENU_ID)).isEqualTo("Race Item");
            assertThat(jdbc.queryForObject("SELECT deleted_at IS NOT NULL FROM menu_items WHERE id=?", Boolean.class, MENU_ID)).isTrue();
            assertThat(jdbc.queryForObject("SELECT count(*) FROM manager_operations WHERE resource_id=?", Integer.class, MENU_ID)).isOne();
        } finally { release.countDown(); executor.shutdownNow(); }
    }

    private void pay() {
        payments.pay(new com.buffetrestaurant.dto.request.CreatePaymentRequest(SESSION_ID,
                com.buffetrestaurant.domain.enums.PaymentMethod.CASH));
    }

    private void waitForSessionLockWait() throws Exception { waitForSessionLockWait(1); }
    private void waitForSessionLockWait(int minimum) throws Exception { waitForLockWait("dining_sessions", minimum); }
    private void waitForLockWait(String table, int minimum) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            Integer blocked = jdbc.queryForObject(
                    "SELECT count(*) FROM pg_stat_activity WHERE datname = current_database() "
                    + "AND pid <> pg_backend_pid() AND state = 'active' AND wait_event_type = 'Lock' "
                    + "AND query LIKE ?",
                    Integer.class, "%" + table + "%");
            if (blocked != null && blocked >= minimum) return;
            Thread.sleep(25);
        }
        throw new AssertionError("Worker did not reach the database lock on " + table);
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) throw new AssertionError("Timed out waiting for test transaction");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError(exception);
        }
    }
}
