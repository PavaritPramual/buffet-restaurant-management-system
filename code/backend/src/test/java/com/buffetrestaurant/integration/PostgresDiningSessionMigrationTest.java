package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/** Runs against a disposable PostgreSQL database when DINING_TEST_PG_URL is set. */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "DINING_TEST_PG_URL", matches = ".+")
class PostgresDiningSessionMigrationTest {
    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> System.getenv("DINING_TEST_PG_URL"));
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.datasource.username", () -> "postgres");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.flyway.locations", () ->
                "classpath:db/migration/common,classpath:db/migration/postgresql");
    }

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void flywayThroughV8AndHibernateValidationStartOnPostgres() {
        assertThat(jdbc.queryForObject(
                "SELECT count(*) FROM public.flyway_schema_history WHERE version = '8' AND success",
                Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject(
                "SELECT is_nullable FROM information_schema.columns "
                        + "WHERE table_schema = 'public' AND table_name = 'dining_sessions' "
                        + "AND column_name = 'package_price_at_open'",
                String.class)).isEqualTo("NO");
    }
}
