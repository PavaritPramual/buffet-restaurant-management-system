package com.buffetrestaurant.integration;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Runs only against a throwaway container; never point this at the shared central database. */
@Testcontainers(disabledWithoutDocker = true)
class PostgresStockProfileMigrationTest {
    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("buffet")
            .withUsername("postgres")
            .withPassword("backend-test")
            .withInitScript("postgres-test-roles.sql");

    @Test
    void postgresV15AddsTargetActiveAndProfileColumnsWithoutChangingLegacyRows() {
        StockProfileMigrationAssertions.assertV15PreservesLegacyRows(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(),
                POSTGRES.getPassword(), "classpath:db/migration/common", "classpath:db/migration/postgresql");
    }
}
