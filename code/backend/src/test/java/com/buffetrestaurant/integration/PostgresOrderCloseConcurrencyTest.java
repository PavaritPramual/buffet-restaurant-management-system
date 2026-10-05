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

@SpringBootTest(properties = "app.ordering.session-provider=database")
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

    private void waitForSessionLockWait() throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            Integer blocked = jdbc.queryForObject(
                    "SELECT count(*) FROM pg_stat_activity WHERE datname = current_database() "
                    + "AND pid <> pg_backend_pid() AND state = 'active' AND wait_event_type = 'Lock' "
                    + "AND query LIKE '%dining_sessions%'",
                    Integer.class);
            if (blocked != null && blocked > 0) return;
            Thread.sleep(25);
        }
        throw new AssertionError("Worker did not reach the database session lock");
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
