package com.buffetrestaurant.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/** Runs the same reporting/authorization scenarios against disposable PostgreSQL. */
@SpringBootTest(properties = {"spring.config.import=", "app.fulfillment.access-provider=session",
        "app.billing.context-provider=database", "app.payment.status-provider=database"})
@EnabledIfEnvironmentVariable(named = "DASHBOARD_TEST_PG_URL", matches = ".+")
@EnabledIfEnvironmentVariable(named = "ALLOW_DESTRUCTIVE_DB_TESTS", matches = "true")
class PostgresDashboardIntegrationTest extends DashboardScenarios {
    @DynamicPropertySource
    static void postgres(DynamicPropertyRegistry properties) {
        String url = DisposablePostgresDatabase.requireReady("DASHBOARD_TEST");
        properties.add("spring.datasource.url", () -> url);
        properties.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        properties.add("spring.datasource.username", () -> System.getenv().getOrDefault("DASHBOARD_TEST_PG_USER", "postgres"));
        properties.add("spring.datasource.password", () -> System.getenv().getOrDefault("DASHBOARD_TEST_PG_PASSWORD", ""));
        properties.add("spring.flyway.locations", () -> "classpath:db/migration/common,classpath:db/migration/postgresql");
    }
}
