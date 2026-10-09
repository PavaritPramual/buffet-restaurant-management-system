package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.*;
import com.buffetrestaurant.dto.request.OpenDiningSessionRequest;
import com.buffetrestaurant.service.*;
import com.buffetrestaurant.repository.RestaurantTableRepository;
import com.buffetrestaurant.exception.*;
import java.time.Duration;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

/** Actual PostgreSQL row locks; authentication here is mocked, proven separately through HTTP tests. */
@SpringBootTest
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named="DINING_TEST_PG_URL", matches=".+")
@EnabledIfEnvironmentVariable(named="ALLOW_DESTRUCTIVE_DB_TESTS", matches="true")
class PostgresMasterDataRemovalConcurrencyTest {
    static final long ID = 970001;
    @DynamicPropertySource static void postgres(DynamicPropertyRegistry r) {
        String url = DisposablePostgresDatabase.requireReady("DINING_TEST");
        r.add("spring.datasource.url", () -> url);
        r.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        r.add("spring.datasource.username", () -> System.getenv().getOrDefault("DINING_TEST_PG_USER", "postgres"));
        r.add("spring.datasource.password", () -> System.getenv().getOrDefault("DINING_TEST_PG_PASSWORD", ""));
        r.add("spring.flyway.locations", () -> "classpath:db/migration/common,classpath:db/migration/postgresql");
    }
    @Autowired JdbcTemplate jdbc;
    @Autowired TransactionTemplate tx;
    @Autowired RestaurantTableService tables;
    @Autowired RestaurantTableRepository tableRepository;
    @Autowired DiningSessionService sessions;
    @Autowired CatalogService catalog;
    @MockitoBean MasterDataRemovalAccessProvider removalAccess;
    @MockitoBean DiningSessionStaffAccessProvider staffAccess;

    @BeforeEach void seed() {
        clean();
        jdbc.update("INSERT INTO restaurant_tables(id,table_number,capacity,status) VALUES (?,'RACE-ARCH',4,'AVAILABLE')",ID);
        jdbc.update("INSERT INTO buffet_packages(id,name,price,active) VALUES (?,'Race package',299,true)",ID);
        jdbc.update("INSERT INTO soups(id,name,active) VALUES (?,'Race soup',true)",ID);
    }
    @AfterEach void clean() {
        jdbc.update("DELETE FROM customer_session_grants WHERE session_id IN (SELECT id FROM dining_sessions WHERE table_id=?)",ID);
        jdbc.update("DELETE FROM dining_sessions WHERE table_id=?",ID);
        jdbc.update("DELETE FROM restaurant_tables WHERE id=?",ID);
        jdbc.update("DELETE FROM buffet_packages WHERE id=?",ID);
        jdbc.update("DELETE FROM soups WHERE id=?",ID);
    }
    private void history() {
        jdbc.update("INSERT INTO dining_sessions(table_id,package_id,soup_id,adult_count,child_count,package_price_at_open,session_token,status) VALUES (?,?,?,1,0,299,'removal-old','COMPLETED')",ID,ID,ID);
    }
    private void open() { sessions.openSession(new OpenDiningSessionRequest(ID, ID, ID, 1, 0)); }
    private void await(CountDownLatch latch) {
        try { if (!latch.await(8,TimeUnit.SECONDS)) throw new AssertionError("Latch timed out"); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new AssertionError(e); }
    }
    private void awaitLock() {
        long end = System.nanoTime()+Duration.ofSeconds(5).toNanos();
        while(System.nanoTime()<end) {
            if (jdbc.queryForObject("SELECT count(*) FROM pg_stat_activity WHERE datname=current_database() AND wait_event_type='Lock' AND pid<>pg_backend_pid()",Integer.class)>0) return;
            try { Thread.sleep(10); } catch(InterruptedException e) { Thread.currentThread().interrupt(); throw new AssertionError(e); }
        }
        throw new AssertionError("Second transaction did not wait on the PostgreSQL row lock");
    }
    private void race(Runnable first, Runnable second) throws Exception {
        CountDownLatch held = new CountDownLatch(1), release = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<?> a = pool.submit(() -> tx.executeWithoutResult(s -> { first.run(); held.countDown(); await(release); }));
            await(held);
            Future<?> b = pool.submit(second);
            awaitLock(); release.countDown();
            a.get(8,TimeUnit.SECONDS); b.get(8,TimeUnit.SECONDS);
        } finally { release.countDown(); pool.shutdownNow(); }
    }
    @Test void openFirstMakesLaterTableRemovalConflict() throws Exception {
        race(this::open, () -> assertThatThrownBy(() -> tables.deleteTable(ID)).isInstanceOf(DuplicateResourceException.class));
        assertThat(jdbc.queryForObject("SELECT status FROM restaurant_tables WHERE id=?",String.class,ID)).isEqualTo("OCCUPIED");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM dining_sessions WHERE table_id=? AND status='ACTIVE'",Integer.class,ID)).isOne();
    }
    @Test void archiveFirstRejectsLaterOpenWithoutLosingHistory() throws Exception {
        history();
        race(() -> tables.deleteTable(ID), () -> assertThatThrownBy(this::open).isInstanceOf(IllegalStateException.class));
        assertThat(jdbc.queryForObject("SELECT archived FROM restaurant_tables WHERE id=?",Boolean.class,ID)).isTrue();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM dining_sessions WHERE table_id=?",Integer.class,ID)).isOne();
    }
    @Test void hardDeleteFirstMakesLaterOpenNotFound() throws Exception {
        race(() -> tables.deleteTable(ID), () -> assertThatThrownBy(this::open).isInstanceOf(ResourceNotFoundException.class));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM dining_sessions WHERE table_id=?",Integer.class,ID)).isZero();
    }
    @Test void packageArchiveFirstRejectsNewOpen() throws Exception {
        history();
        race(() -> catalog.removePackage(ID), () -> assertThatThrownBy(this::open).isInstanceOf(IllegalStateException.class));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM dining_sessions WHERE table_id=? AND status='ACTIVE'",Integer.class,ID)).isZero();
    }
    @Test void soupArchiveFirstRejectsNewOpen() throws Exception {
        history();
        race(() -> catalog.removeSoup(ID), () -> assertThatThrownBy(this::open).isInstanceOf(IllegalStateException.class));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM dining_sessions WHERE table_id=? AND status='ACTIVE'",Integer.class,ID)).isZero();
    }
    @Test void openFirstAllowsPackageArchiveButKeepsNewSessionSnapshot() throws Exception {
        race(this::open, () -> catalog.removePackage(ID));
        assertThat(jdbc.queryForObject("SELECT archived FROM buffet_packages WHERE id=?",Boolean.class,ID)).isTrue();
        assertThat(jdbc.queryForObject("SELECT package_price_at_open FROM dining_sessions WHERE table_id=?",java.math.BigDecimal.class,ID)).isEqualByComparingTo("299");
    }
    @Test void unexpectedFkMakesHardDeleteRollbackRatherThanDroppingReferences() {
        jdbc.execute("CREATE TABLE r01_test_reference (table_id BIGINT REFERENCES restaurant_tables(id) ON DELETE RESTRICT)");
        try {
            jdbc.update("INSERT INTO r01_test_reference VALUES (?)", ID);
            assertThatThrownBy(() -> tables.deleteTable(ID)).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
            assertThat(jdbc.queryForObject("SELECT count(*) FROM restaurant_tables WHERE id=?",Integer.class,ID)).isOne();
            assertThat(jdbc.queryForObject("SELECT count(*) FROM r01_test_reference",Integer.class)).isOne();
        } finally { jdbc.execute("DROP TABLE r01_test_reference"); }
    }
}
