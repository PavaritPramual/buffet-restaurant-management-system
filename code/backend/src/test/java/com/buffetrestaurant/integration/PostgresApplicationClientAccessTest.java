package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "DINING_TEST_PG_URL", matches = ".+")
@EnabledIfEnvironmentVariable(named = "ALLOW_DESTRUCTIVE_DB_TESTS", matches = "true")
class PostgresApplicationClientAccessTest {
    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> DisposablePostgresDatabase.requireReady("DINING_TEST"));
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.datasource.username", () -> System.getenv().getOrDefault("DINING_TEST_PG_USER", "postgres"));
        registry.add("spring.datasource.password", () -> System.getenv().getOrDefault("DINING_TEST_PG_PASSWORD", ""));
        registry.add("spring.flyway.locations", () -> "classpath:db/migration/common,classpath:db/migration/postgresql");
    }

    @Autowired private JdbcTemplate jdbc;

    @Test
    void realClientRolesCannotReadApplicationTablesHistoryOrUseTableSequence() {
        jdbc.execute((org.springframework.jdbc.core.ConnectionCallback<Void>) connection -> {
            try (var statement = connection.createStatement()) {
                for (String role : new String[] {"anon", "authenticated"}) {
                    statement.execute("SET ROLE " + role);
                    try {
                        for (String query : new String[] {"SELECT id FROM restaurant_tables", "SELECT installed_rank FROM flyway_schema_history", "SELECT nextval('restaurant_tables_id_seq')"}) {
                            var error = assertThrows(java.sql.SQLException.class, () -> statement.executeQuery(query));
                            assertThat(error.getSQLState()).isEqualTo("42501");
                        }
                    } finally { statement.execute("RESET ROLE"); }
                }
            }
            return null;
        });
    }

    @Test
    void v13RemovesAllClientTableAndSequencePrivilegesIncludingInheritedPublicGrants() {
        assertThat(jdbc.queryForObject("SELECT count(*) FROM flyway_schema_history WHERE version='13' AND success", Integer.class)).isEqualTo(1);
        for (String role : new String[] {"anon", "authenticated"}) {
            for (String table : new String[] {"restaurant_tables", "buffet_packages", "soups", "menu_categories", "menu_items", "package_menu_items", "dining_sessions", "customer_session_grants", "orders", "order_items", "payments", "app_users", "user_profiles", "stock_items", "stock_transactions", "flyway_schema_history"}) {
                for (String privilege : new String[] {"SELECT", "INSERT", "UPDATE", "DELETE", "TRUNCATE", "REFERENCES", "TRIGGER"}) {
                    assertThat(jdbc.queryForObject("SELECT has_table_privilege(?, ?, ?)", Boolean.class, role, "public." + table, privilege)).as("%s %s %s", role, table, privilege).isFalse();
                }
            }
            assertThat(jdbc.queryForObject("SELECT count(*) FROM pg_class c JOIN pg_namespace n ON n.oid=c.relnamespace WHERE n.nspname='public' AND c.relkind='S' AND (has_sequence_privilege(?,c.oid,'USAGE') OR has_sequence_privilege(?,c.oid,'SELECT') OR has_sequence_privilege(?,c.oid,'UPDATE'))", Integer.class, role, role, role)).isZero();
        }
        assertThat(jdbc.queryForObject("SELECT relrowsecurity FROM pg_class WHERE oid='public.restaurant_tables'::regclass", Boolean.class)).isTrue();
        assertThat(jdbc.queryForObject("SELECT has_table_privilege(current_user,'public.restaurant_tables','INSERT')", Boolean.class)).isTrue();
        assertThat(jdbc.queryForObject("SELECT has_sequence_privilege(current_user,pg_get_serial_sequence('public.restaurant_tables','id'),'USAGE')", Boolean.class)).isTrue();
    }
}
