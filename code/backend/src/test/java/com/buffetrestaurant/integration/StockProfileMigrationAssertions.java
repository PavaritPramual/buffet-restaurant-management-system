package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.Map;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/** Shared V15 upgrade checks run against both H2 and a disposable PostgreSQL database. */
final class StockProfileMigrationAssertions {
    private StockProfileMigrationAssertions() {
    }

    static void assertV15PreservesLegacyRows(String url, String user, String password, String... locations) {
        migrate(url, user, password, "14", locations);
        DataSource dataSource = new DriverManagerDataSource(url, user, password);
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.update("INSERT INTO app_users (username, password_hash, role) VALUES ('legacy', 'hash', 'MANAGER')");
        Long userId = jdbc.queryForObject("SELECT id FROM app_users WHERE username = 'legacy'", Long.class);
        jdbc.update("INSERT INTO user_profiles (user_id, display_name, email) VALUES (?, 'Legacy Name', 'l@example.test')", userId);
        jdbc.update("INSERT INTO stock_items (sku, name, unit, quantity, low_stock_threshold) "
                + "VALUES ('OLD-1', 'Old rice', 'kg', 7.500, 2.000)");
        jdbc.update("INSERT INTO stock_transactions (stock_item_id, transaction_type, quantity_delta, balance_after, reason, actor_user_id) "
                + "SELECT id, 'IN', 7.500, 7.500, 'legacy delivery', ? FROM stock_items WHERE sku = 'OLD-1'", userId);

        migrate(url, user, password, null, locations);

        Map<String, Object> item = jdbc.queryForMap("SELECT * FROM stock_items WHERE sku = 'OLD-1'");
        assertThat((BigDecimal) item.get("quantity")).isEqualByComparingTo("7.500");
        assertThat((BigDecimal) item.get("low_stock_threshold")).isEqualByComparingTo("2.000");
        assertThat((BigDecimal) item.get("opening_target_stock")).isEqualByComparingTo("0");
        assertThat(item.get("active")).isEqualTo(true);
        Map<String, Object> profile = jdbc.queryForMap("SELECT * FROM user_profiles WHERE user_id = ?", userId);
        assertThat(profile.get("display_name")).isEqualTo("Legacy Name");
        assertThat(profile.get("email")).isEqualTo("l@example.test");
        assertThat(profile.get("first_name")).isNull();
        assertThat(profile.get("last_name")).isNull();
        assertThat(profile.get("phone_number")).isNull();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM stock_transactions", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE version = '15' AND success = TRUE",
                Integer.class)).isEqualTo(1);

        assertThatThrownBy(() -> jdbc.update("INSERT INTO stock_items (sku, name, unit, quantity, low_stock_threshold, opening_target_stock) "
                + "VALUES ('NEG', 'Bad', 'kg', 0, 0, -1)")).isInstanceOf(Exception.class);
        assertThatThrownBy(() -> jdbc.update("UPDATE user_profiles SET first_name = ? WHERE user_id = ?",
                "f".repeat(101), userId)).isInstanceOf(Exception.class);
        assertThatThrownBy(() -> jdbc.update("UPDATE user_profiles SET phone_number = ? WHERE user_id = ?",
                "1".repeat(21), userId)).isInstanceOf(Exception.class);
    }

    private static void migrate(String url, String user, String password, String target, String... locations) {
        var configuration = Flyway.configure().dataSource(url, user, password).locations(locations);
        if (target != null) configuration.target(target);
        configuration.load().migrate();
    }
}
