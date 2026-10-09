package com.buffetrestaurant.integration;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Re-runs the stock/user archive scenarios against a throwaway PostgreSQL container. */
@ActiveProfiles({"test", "postgres-it"})
@Testcontainers(disabledWithoutDocker = true)
class PostgresStockUserArchiveIntegrationTest extends StockUserArchiveIntegrationTest {
    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("buffet")
            .withUsername("postgres")
            .withPassword("backend-test")
            .withInitScript("postgres-test-roles.sql");

    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        properties.add("spring.datasource.username", POSTGRES::getUsername);
        properties.add("spring.datasource.password", POSTGRES::getPassword);
        properties.add("spring.flyway.locations",
                () -> "classpath:db/migration/common,classpath:db/migration/postgresql");
        properties.add("spring.datasource.hikari.maximum-pool-size", () -> 20);
    }
}
