package com.buffetrestaurant.integration;

import com.buffetrestaurant.controller.AuthController;
import com.buffetrestaurant.domain.enums.UserRole;
import com.buffetrestaurant.dto.response.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/** Runs the same payment acceptance cases on an explicitly marked disposable PostgreSQL DB. */
@EnabledIfEnvironmentVariable(named = "PAYMENT_TEST_PG_URL", matches = ".+")
@EnabledIfEnvironmentVariable(named = "ALLOW_DESTRUCTIVE_DB_TESTS", matches = "true")
class PostgresPaymentIntegrationTest extends PaymentIntegrationTest {
    @Autowired JdbcTemplate jdbc;
    @Autowired MockMvc mvc;

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry properties) {
        String url = DisposablePostgresDatabase.requireReady("PAYMENT_TEST");
        properties.add("spring.datasource.url", () -> url);
        properties.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        properties.add("spring.datasource.username", () -> System.getenv().getOrDefault("PAYMENT_TEST_PG_USER", "postgres"));
        properties.add("spring.datasource.password", () -> System.getenv().getOrDefault("PAYMENT_TEST_PG_PASSWORD", ""));
        properties.add("spring.flyway.locations", () -> "classpath:db/migration/common,classpath:db/migration/postgresql");
    }

    @Test
    void backendRoleCanAccessButClientsCannot() {
        String user = jdbc.queryForObject("SELECT current_user", String.class);
        assertThat(jdbc.queryForObject("SELECT has_table_privilege(current_user, 'public.payments', 'INSERT')", Boolean.class)).isTrue();
        assertThat(jdbc.queryForObject("SELECT has_sequence_privilege(current_user, pg_get_serial_sequence('public.payments','id'), 'USAGE')", Boolean.class)).isTrue();
        assertThat(jdbc.queryForObject("SELECT relrowsecurity FROM pg_class WHERE oid='public.payments'::regclass", Boolean.class)).isTrue();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM pg_policies WHERE tablename='payments' AND ? = ANY(roles::text[])", Integer.class, user)).isEqualTo(1);
        for (String role : List.of("anon", "authenticated")) {
            assertThat(jdbc.queryForObject("SELECT has_table_privilege(?, 'public.payments', 'SELECT')", Boolean.class, role)).isFalse();
            assertThat(jdbc.queryForObject("SELECT has_sequence_privilege(?, pg_get_serial_sequence('public.payments','id'), 'USAGE')", Boolean.class, role)).isFalse();
        }
    }

    @Test
    void databaseRejectsInvalidAmountMethodTimestampAndForeignKey() {
        jdbc.execute((org.springframework.jdbc.core.ConnectionCallback<Void>) connection -> {
            for (String values : List.of(
                    "918001,-1,'CASH','PENDING',NULL",
                    "918001,1,'UNKNOWN','PENDING',NULL",
                    "918001,1,'CASH','PAID',NULL",
                    "918001,1,'CASH','PENDING',CURRENT_TIMESTAMP",
                    "999999,1,'CASH','PENDING',NULL")) {
                var savepoint = connection.setSavepoint();
                try (var statement = connection.createStatement()) {
                    assertThatThrownBy(() -> statement.executeUpdate(
                            "INSERT INTO payments(session_id,amount,payment_method,payment_status,paid_at) VALUES (" + values + ")"))
                            .isInstanceOf(java.sql.SQLException.class);
                } finally { connection.rollback(savepoint); }
            }
            return null;
        });
        assertThat(jdbc.queryForObject("SELECT count(*) FROM payments WHERE session_id=918001", Integer.class)).isZero();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void concurrentRequestsRecordOnePayment() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Callable<Integer> request = () -> {
                if (!start.await(10, TimeUnit.SECONDS)) throw new AssertionError("Start timed out");
                MockHttpSession session = new MockHttpSession();
                session.setAttribute(AuthController.USER_CONTEXT_SESSION_KEY,
                        new UserContext(1L, "staff", "Staff", UserRole.SERVICE_STAFF));
                return mvc.perform(post("/api/v1/payments").session(session)
                        .contentType("application/json")
                        .content("{\"sessionId\":918001,\"paymentMethod\":\"CASH\"}"))
                        .andReturn().getResponse().getStatus();
            };
            Future<Integer> first = pool.submit(request);
            Future<Integer> second = pool.submit(request);
            start.countDown();
            assertThat(List.of(first.get(20, TimeUnit.SECONDS), second.get(20, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(201, 409);
            assertThat(jdbc.queryForObject("SELECT count(*) FROM payments WHERE session_id=918001", Integer.class)).isEqualTo(1);
            assertThat(jdbc.queryForObject("SELECT amount FROM payments WHERE session_id=918001", java.math.BigDecimal.class)).isEqualByComparingTo("997.50");
        } finally { pool.shutdownNow(); pool.awaitTermination(10, TimeUnit.SECONDS); }
    }

    @AfterEach
    void cleanup() {
        // Same safety gate as startup, before any destructive cleanup.
        DisposablePostgresDatabase.requireReady("PAYMENT_TEST");
        jdbc.execute("DELETE FROM payments WHERE session_id=918001");
        jdbc.execute("DELETE FROM dining_sessions WHERE id=918001");
        jdbc.execute("DELETE FROM restaurant_tables WHERE id=918001");
        jdbc.execute("DELETE FROM buffet_packages WHERE id=918001");
        jdbc.execute("DELETE FROM soups WHERE id=918001");
    }
}
