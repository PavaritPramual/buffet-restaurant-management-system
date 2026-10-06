package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.dao.DataIntegrityViolationException;

class DiningSessionMigrationTest {
    @Test
    void freshSchemaAppliesDiningSessionMigrationsWithExpectedConstraints() {
        String url = "jdbc:h2:mem:dining_session_migration_" + UUID.randomUUID()
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/migration/common", "classpath:db/migration/h2")
                .load()
                .migrate();

        JdbcTemplate jdbc = new JdbcTemplate(new DriverManagerDataSource(url, "sa", ""));
        assertThat(jdbc.queryForObject("SELECT data_type FROM information_schema.columns WHERE lower(table_name)='dining_sessions' AND lower(column_name)='bill_requested_at'", String.class)).isEqualTo("timestamp with time zone");
        assertThat(jdbc.queryForObject("SELECT is_nullable FROM information_schema.columns WHERE lower(table_name)='dining_sessions' AND lower(column_name)='bill_requested_at'", String.class)).isEqualTo("YES");
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.table_constraints "
                        + "WHERE lower(table_name) = 'dining_sessions' AND constraint_type = 'FOREIGN KEY'",
                Integer.class)).isEqualTo(3);
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.table_constraints "
                        + "WHERE lower(table_name) = 'dining_sessions' AND constraint_type = 'UNIQUE'",
                Integer.class)).isEqualTo(1);
        for (String index : new String[] {"idx_dining_sessions_table", "idx_dining_sessions_package",
                "idx_dining_sessions_soup", "idx_customer_grants_session", "idx_orders_session"}) {
            assertThat(jdbc.queryForObject("SELECT count(*) FROM information_schema.indexes "
                    + "WHERE lower(index_name) = ?", Integer.class, index)).isOne();
        }
        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO orders (session_id, table_number, status) VALUES (?, ?, ?)",
                999L, "T99", "RECEIVED"))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(jdbc.queryForObject(
                "SELECT column_default FROM information_schema.columns "
                        + "WHERE lower(table_name) = 'dining_sessions' AND lower(column_name) = 'child_count'",
                String.class)).contains("0");
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE version = '6' AND success = TRUE",
                Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE version = '7' AND success = TRUE",
                Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE version = '8' AND success = TRUE",
                Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject(
                "SELECT is_nullable FROM information_schema.columns "
                        + "WHERE lower(table_name) = 'dining_sessions' "
                        + "AND lower(column_name) = 'package_price_at_open'",
                String.class)).isEqualTo("NO");
        for (String column : new String[] {"buffet_packages.price", "dining_sessions.package_price_at_open"}) {
            String[] parts = column.split("\\.");
            assertThat(jdbc.queryForObject(
                    "SELECT numeric_precision FROM information_schema.columns "
                            + "WHERE lower(table_name) = ? AND lower(column_name) = ?",
                    Integer.class, parts[0], parts[1])).isEqualTo(10);
            assertThat(jdbc.queryForObject(
                    "SELECT numeric_scale FROM information_schema.columns "
                            + "WHERE lower(table_name) = ? AND lower(column_name) = ?",
                    Integer.class, parts[0], parts[1])).isEqualTo(2);
        }
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.table_constraints "
                        + "WHERE lower(table_name) = 'orders' AND lower(constraint_name) = 'fk_orders_dining_session' "
                        + "AND constraint_type = 'FOREIGN KEY'",
                Integer.class)).isEqualTo(1);

        jdbc.update("INSERT INTO restaurant_tables (id, table_number, capacity, status) "
                + "VALUES (9001, 'V7-RESTRICT', 4, 'OCCUPIED')");
        jdbc.update("INSERT INTO buffet_packages (id, name, price) VALUES (9001, 'V7 Package', 299)");
        jdbc.update("INSERT INTO soups (id, name) VALUES (9001, 'V7 Soup')");
        jdbc.update("INSERT INTO dining_sessions "
                + "(id, table_id, package_id, soup_id, adult_count, package_price_at_open, session_token) "
                + "VALUES (9001, 9001, 9001, 9001, 1, 299, 'v7-restrict-token')");
        jdbc.update("INSERT INTO orders (id, session_id, table_number) "
                + "VALUES (9001, 9001, 'V7-RESTRICT')");
        assertThatThrownBy(() -> jdbc.update("DELETE FROM dining_sessions WHERE id = 9001"))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM orders WHERE id = 9001", Integer.class)).isOne();
        assertThatThrownBy(() -> jdbc.update("UPDATE dining_sessions SET adult_count = 0 WHERE id = 9001"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void v8BackfillsExistingSessionsFromCatalogPrice() {
        String url = "jdbc:h2:mem:dining_session_upgrade_" + UUID.randomUUID()
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/migration/common", "classpath:db/migration/h2")
                .target(MigrationVersion.fromVersion("7"))
                .load()
                .migrate();
        JdbcTemplate jdbc = new JdbcTemplate(new DriverManagerDataSource(url, "sa", ""));
        jdbc.update("INSERT INTO restaurant_tables (id, table_number, capacity) "
                + "VALUES (9101, 'V8-UPGRADE', 4)");
        jdbc.update("INSERT INTO buffet_packages (id, name, price) "
                + "VALUES (9101, 'Upgrade Package', 299)");
        jdbc.update("INSERT INTO soups (id, name) VALUES (9101, 'Upgrade Soup')");
        jdbc.update("INSERT INTO dining_sessions "
                + "(id, table_id, package_id, soup_id, adult_count, session_token) "
                + "VALUES (9101, 9101, 9101, 9101, 2, 'v8-upgrade-token')");
        jdbc.update("UPDATE buffet_packages SET price = 399 WHERE id = 9101");

        Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/migration/common", "classpath:db/migration/h2")
                .load()
                .migrate();

        assertThat(jdbc.queryForObject(
                "SELECT package_price_at_open FROM dining_sessions WHERE id = 9101",
                java.math.BigDecimal.class)).isEqualByComparingTo("399.00");
        assertThatThrownBy(() -> jdbc.update("UPDATE dining_sessions "
                + "SET package_price_at_open = -1 WHERE id = 9101"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
