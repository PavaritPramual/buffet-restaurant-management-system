package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.*;

import com.buffetrestaurant.dto.request.*;
import com.buffetrestaurant.exception.MenuConflictException;
import com.buffetrestaurant.service.*;
import com.buffetrestaurant.service.impl.CustomerSessionAccessService;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

/** Real PostgreSQL row-lock races. Authorization is covered separately with real sessions. */
@SpringBootTest(properties="app.ordering.session-provider=database")
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named="DINING_TEST_PG_URL", matches=".+")
@EnabledIfEnvironmentVariable(named="ALLOW_DESTRUCTIVE_DB_TESTS", matches="true")
class PostgresMenuArchiveConcurrencyTest {
    private static final long ID = 961001L;
    @DynamicPropertySource static void postgres(DynamicPropertyRegistry registry) {
        String url = DisposablePostgresDatabase.requireReady("DINING_TEST");
        registry.add("spring.datasource.url", () -> url);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.datasource.username", () -> System.getenv().getOrDefault("DINING_TEST_PG_USER","postgres"));
        registry.add("spring.datasource.password", () -> System.getenv().getOrDefault("DINING_TEST_PG_PASSWORD",""));
        registry.add("spring.flyway.locations", () -> "classpath:db/migration/common,classpath:db/migration/postgresql");
    }
    @Autowired JdbcTemplate jdbc;
    @Autowired TransactionTemplate transactions;
    @Autowired MenuCatalogService catalog;
    @Autowired CustomerOrderingService ordering;
    @Autowired CustomerSessionAccessService access;
    @MockitoBean MenuAdminAccessProvider manager;
    String credential;

    @BeforeEach void seed() {
        clean();
        jdbc.update("INSERT INTO buffet_packages(id,name,price,active) VALUES (961001,'Archive race package',299,true)");
        jdbc.update("INSERT INTO soups(id,name,active) VALUES (961001,'Archive race soup',true)");
        jdbc.update("INSERT INTO restaurant_tables(id,table_number,capacity,status) VALUES (961001,'AR-RACE',4,'OCCUPIED')");
        jdbc.update("INSERT INTO dining_sessions(id,table_id,package_id,soup_id,adult_count,child_count,package_price_at_open,session_token,status) "
                + "VALUES (961001,961001,961001,961001,2,0,299,'archive-race-qr','ACTIVE')");
        jdbc.update("INSERT INTO menu_categories(id,name) VALUES (961001,'Archive race category')");
        jdbc.update("INSERT INTO menu_items(id,category_id,name,available) VALUES (961001,961001,'Archive race item',true)");
        jdbc.update("INSERT INTO package_menu_items(package_id,menu_item_id) VALUES (961001,961001)");
        credential = access.exchange("archive-race-qr").credential();
    }
    @AfterEach void clean() {
        String url = DisposablePostgresDatabase.requireUrl("DINING_TEST",System.getenv());
        jdbc.execute((ConnectionCallback<Void>) connection -> { DisposablePostgresDatabase.verifyDatabase(connection,url); return null; });
        jdbc.update("DELETE FROM order_items WHERE order_id IN (SELECT id FROM orders WHERE session_id=961001)");
        jdbc.update("DELETE FROM orders WHERE session_id=961001");
        jdbc.update("DELETE FROM customer_session_grants WHERE session_id=961001");
        jdbc.update("DELETE FROM package_menu_items WHERE package_id=961001");
        jdbc.update("DELETE FROM menu_items WHERE category_id=961001");
        jdbc.update("DELETE FROM menu_categories WHERE id=961001");
        jdbc.update("DELETE FROM dining_sessions WHERE id=961001");
        jdbc.update("DELETE FROM restaurant_tables WHERE id=961001");
        jdbc.update("DELETE FROM soups WHERE id=961001");
        jdbc.update("DELETE FROM buffet_packages WHERE id=961001");
    }

    @Test void orderCommitsBeforeWaitingRemovalWhichArchivesAndPreservesTheOrder() throws Exception {
        race(() -> ordering.placeOrder(ID,credential,order()), () -> catalog.deleteMenuItem(ID), "menu_items");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM orders WHERE session_id=961001",Integer.class)).isOne();
        assertThat(jdbc.queryForObject("SELECT item_name FROM order_items WHERE menu_item_id=961001",String.class)).isEqualTo("Archive race item");
        assertThat(jdbc.queryForObject("SELECT archived_at IS NOT NULL FROM menu_items WHERE id=961001",Boolean.class)).isTrue();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM package_menu_items WHERE menu_item_id=961001",Integer.class)).isOne();
    }

    @Test void archiveCommitsBeforeWaitingOrderWhichCannotPersistAnythingNew() throws Exception {
        ordering.placeOrder(ID,credential,order()); // existing history forces archive
        race(() -> catalog.deleteMenuItem(ID), () -> assertThatThrownBy(() -> ordering.placeOrder(ID,credential,order()))
                .isInstanceOf(MenuConflictException.class), "menu_items");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM orders WHERE session_id=961001",Integer.class)).isOne();
    }

    @Test void creationHoldingParentLockPreventsConcurrentCategoryDeletion() throws Exception {
        jdbc.update("DELETE FROM package_menu_items WHERE menu_item_id=961001");
        jdbc.update("DELETE FROM menu_items WHERE id=961001");
        race(() -> catalog.createMenuItem(new MenuItemRequest(ID,"new race child",null,true,Set.of(ID),null)),
                () -> assertThatThrownBy(() -> catalog.deleteCategory(ID)).isInstanceOf(MenuConflictException.class), "menu_categories");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM menu_items WHERE category_id=961001",Integer.class)).isOne();
        assertThat(jdbc.queryForObject("SELECT archived_at IS NULL FROM menu_categories WHERE id=961001",Boolean.class)).isTrue();
    }

    private PlaceOrderRequest order() { return new PlaceOrderRequest(List.of(new OrderItemRequest(ID,1))); }
    private void race(Runnable first, Runnable second, String table) throws Exception {
        CountDownLatch held = new CountDownLatch(1), release = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            var a = CompletableFuture.runAsync(() -> transactions.executeWithoutResult(status -> {
                first.run(); held.countDown(); await(release);
            }),executor);
            assertThat(held.await(10,TimeUnit.SECONDS)).isTrue();
            var b = CompletableFuture.runAsync(second,executor);
            long deadline = System.nanoTime()+TimeUnit.SECONDS.toNanos(10);
            boolean waiting = false;
            while (System.nanoTime()<deadline) {
                Integer count=jdbc.queryForObject("SELECT count(*) FROM pg_stat_activity WHERE datname=current_database() "
                        + "AND pid<>pg_backend_pid() AND state='active' AND wait_event_type='Lock' AND query LIKE ?",Integer.class,"%"+table+"%");
                if (count!=null && count>0) { waiting=true; break; }
                Thread.sleep(25);
            }
            assertThat(waiting).as("second transaction reached PostgreSQL lock").isTrue();
            release.countDown(); a.get(15,TimeUnit.SECONDS); b.get(15,TimeUnit.SECONDS);
        } finally { release.countDown(); executor.shutdownNow(); }
    }
    private static void await(CountDownLatch latch) {
        try { if (!latch.await(20,TimeUnit.SECONDS)) throw new AssertionError("transaction timeout"); }
        catch (InterruptedException error) { Thread.currentThread().interrupt(); throw new AssertionError(error); }
    }
}
