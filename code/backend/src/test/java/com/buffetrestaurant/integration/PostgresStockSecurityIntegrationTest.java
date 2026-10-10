package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.buffetrestaurant.domain.StockItem;
import com.buffetrestaurant.domain.enums.UserRole;
import com.buffetrestaurant.dto.request.CreateUserRequest;
import com.buffetrestaurant.dto.request.StockInRequest;
import com.buffetrestaurant.dto.response.UserContext;
import com.buffetrestaurant.repository.StockItemRepository;
import com.buffetrestaurant.repository.StockTransactionRepository;
import com.buffetrestaurant.repository.UserAccountRepository;
import com.buffetrestaurant.repository.UserProfileRepository;
import com.buffetrestaurant.service.AuthService;
import com.buffetrestaurant.service.StockService;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("postgres-it")
@Testcontainers(disabledWithoutDocker = true)
class PostgresStockSecurityIntegrationTest {
    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("buffet")
            .withUsername("postgres")
            .withPassword("backend-test")
            .withInitScript("postgres-test-roles.sql");

    @Autowired private JdbcTemplate jdbc;
    @Autowired private UserAccountRepository users;
    @Autowired private UserProfileRepository profiles;
    @Autowired private StockItemRepository items;
    @Autowired private StockTransactionRepository transactions;
    @Autowired private AuthService authService;
    @Autowired private StockService stockService;

    private Long itemId;
    private UserContext actor;

    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        properties.add("spring.datasource.username", POSTGRES::getUsername);
        properties.add("spring.datasource.password", POSTGRES::getPassword);
        properties.add("spring.flyway.locations",
                () -> "classpath:db/migration/common,classpath:db/migration/postgresql");
        properties.add("spring.datasource.hikari.maximum-pool-size", () -> 20);
    }

    @BeforeEach
    void setUp() {
        transactions.deleteAll();
        items.deleteAll();
        profiles.deleteAll();
        users.deleteAll();
        var manager = authService.createUser(new CreateUserRequest("postgres-manager", "password123",
                "Postgres Manager", null, UserRole.MANAGER, "Postgres", "Manager", null));
        actor = new UserContext(manager.id(), manager.username(), manager.displayName(), manager.role());
        itemId = items.save(new StockItem("PG-LOCK", "Concurrent rice", "kg",
                BigDecimal.ZERO, BigDecimal.ZERO)).getId();
    }

    @Test
    void v11RevokesClientRoleAccessButKeepsBackendOwnerAccess() {
        for (String role : List.of("anon", "authenticated")) {
            assertThat(hasPrivilege("SELECT", role, "public.flyway_schema_history")).isFalse();
            assertThat(hasPrivilege("SELECT", role, "public.app_users")).isFalse();
            assertThat(hasPrivilege("INSERT", role, "public.stock_items")).isFalse();
            assertThat(jdbc.queryForObject(
                    "SELECT has_sequence_privilege(?, 'public.stock_items_id_seq', 'USAGE')",
                    Boolean.class, role)).isFalse();
        }
        assertThat(jdbc.queryForObject(
                "SELECT has_table_privilege(current_user, 'public.flyway_schema_history', 'SELECT')",
                Boolean.class)).isTrue();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM public.flyway_schema_history", Integer.class))
                .isGreaterThan(0);
    }

    @Test
    void concurrentStockInKeepsBalanceAndEveryAuditSnapshotInSync() throws Exception {
        int operationCount = 16;
        CountDownLatch ready = new CountDownLatch(operationCount);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(operationCount);
        try {
            List<? extends Future<?>> operations = IntStream.range(0, operationCount)
                    .mapToObj(index -> executor.submit(() -> {
                        ready.countDown();
                        if (!start.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("Start gate timed out");
                        stockService.stockIn(itemId, new StockInRequest(BigDecimal.ONE, "Concurrent delivery"), actor);
                        return null;
                    }))
                    .toList();
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            for (Future<?> operation : operations) operation.get(30, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }

        assertThat(items.findById(itemId).orElseThrow().getQuantity())
                .isEqualByComparingTo(BigDecimal.valueOf(operationCount));
        assertThat(transactions.findAllByStockItemIdOrderByCreatedAtDescIdDesc(itemId))
                .hasSize(operationCount);
        List<BigDecimal> recordedBalances = jdbc.queryForList(
                "SELECT balance_after FROM stock_transactions WHERE stock_item_id = ? ORDER BY id",
                BigDecimal.class, itemId);
        List<BigDecimal> expectedBalances = IntStream.rangeClosed(1, operationCount)
                .mapToObj(value -> BigDecimal.valueOf(value).setScale(3))
                .toList();
        assertThat(recordedBalances).containsExactlyElementsOf(expectedBalances);
    }

    private boolean hasPrivilege(String privilege, String role, String table) {
        return jdbc.queryForObject("SELECT has_table_privilege(?, ?, ?)", Boolean.class,
                role, table, privilege);
    }
}