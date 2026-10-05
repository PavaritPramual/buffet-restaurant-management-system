package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/** Requires explicit opt-in and a marked disposable loopback database before Spring/Flyway startup. */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "DINING_TEST_PG_URL", matches = ".+")
@EnabledIfEnvironmentVariable(named = "ALLOW_DESTRUCTIVE_DB_TESTS", matches = "true")
class PostgresDiningSessionMigrationTest {
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
        for (String column : new String[] {"buffet_packages.price", "dining_sessions.package_price_at_open"}) {
            String[] parts = column.split("\\.");
            assertThat(jdbc.queryForObject("SELECT numeric_precision FROM information_schema.columns "
                            + "WHERE table_schema = 'public' AND table_name = ? AND column_name = ?",
                    Integer.class, parts[0], parts[1])).isEqualTo(10);
            assertThat(jdbc.queryForObject("SELECT numeric_scale FROM information_schema.columns "
                            + "WHERE table_schema = 'public' AND table_name = ? AND column_name = ?",
                    Integer.class, parts[0], parts[1])).isEqualTo(2);
        }
        assertThat(jdbc.queryForObject("SELECT count(*) FROM pg_policies "
                + "WHERE schemaname = 'public' AND tablename IN ('dining_sessions', 'customer_session_grants') "
                + "AND 'postgres' = ANY(roles)", Integer.class)).isEqualTo(2);
        for (String table : new String[] {"dining_sessions", "customer_session_grants"}) {
            for (String privilege : new String[] {"SELECT", "INSERT", "UPDATE", "DELETE"}) {
                assertThat(jdbc.queryForObject("SELECT has_table_privilege('anon', ?, ?)", Boolean.class,
                        "public." + table, privilege)).isFalse();
                assertThat(jdbc.queryForObject("SELECT has_table_privilege('authenticated', ?, ?)", Boolean.class,
                        "public." + table, privilege)).isFalse();
                assertThat(jdbc.queryForObject("SELECT has_table_privilege('postgres', ?, ?)", Boolean.class,
                        "public." + table, privilege)).isTrue();
            }
        }
        for (String sequence : new String[] {"dining_sessions_id_seq", "customer_session_grants_id_seq"}) {
            assertThat(jdbc.queryForObject("SELECT has_sequence_privilege('anon', ?, 'USAGE')", Boolean.class,
                    "public." + sequence)).isFalse();
            assertThat(jdbc.queryForObject("SELECT has_sequence_privilege('authenticated', ?, 'USAGE')", Boolean.class,
                    "public." + sequence)).isFalse();
        }
    }
}
