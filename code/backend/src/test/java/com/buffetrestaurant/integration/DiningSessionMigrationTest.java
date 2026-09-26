package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.dao.DataIntegrityViolationException;

class DiningSessionMigrationTest {
    @Test
    void freshSchemaAppliesDiningSessionV6WithExpectedConstraints() {
        String url = "jdbc:h2:mem:dining_session_migration_" + UUID.randomUUID()
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        MigrateResult result = Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/migration/common", "classpath:db/migration/h2")
                .load()
                .migrate();

        assertThat(result.migrationsExecuted).isEqualTo(6);
        JdbcTemplate jdbc = new JdbcTemplate(new DriverManagerDataSource(url, "sa", ""));
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.table_constraints "
                        + "WHERE lower(table_name) = 'dining_sessions' AND constraint_type = 'FOREIGN KEY'",
                Integer.class)).isEqualTo(3);
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.table_constraints "
                        + "WHERE lower(table_name) = 'dining_sessions' AND constraint_type = 'UNIQUE'",
                Integer.class)).isEqualTo(1);
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
                "SELECT COUNT(*) FROM information_schema.table_constraints "
                        + "WHERE lower(table_name) = 'orders' AND lower(constraint_name) = 'fk_orders_dining_session' "
                        + "AND constraint_type = 'FOREIGN KEY'",
                Integer.class)).isEqualTo(1);

        jdbc.update("INSERT INTO restaurant_tables (id, table_number, capacity, status) "
                + "VALUES (9001, 'V7-CASCADE', 4, 'OCCUPIED')");
        jdbc.update("INSERT INTO buffet_packages (id, name, price) VALUES (9001, 'V7 Package', 299)");
        jdbc.update("INSERT INTO soups (id, name) VALUES (9001, 'V7 Soup')");
        jdbc.update("INSERT INTO dining_sessions "
                + "(id, table_id, package_id, soup_id, adult_count, session_token) "
                + "VALUES (9001, 9001, 9001, 9001, 1, 'v7-cascade-token')");
        jdbc.update("INSERT INTO orders (id, session_id, table_number) "
                + "VALUES (9001, 9001, 'V7-CASCADE')");
        jdbc.update("DELETE FROM dining_sessions WHERE id = 9001");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM orders WHERE id = 9001", Integer.class)).isZero();
    }
}
