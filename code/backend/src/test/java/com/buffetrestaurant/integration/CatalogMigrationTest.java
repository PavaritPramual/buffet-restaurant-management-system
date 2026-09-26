package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class CatalogMigrationTest {
    @Test
    void existingV2SchemaUpgradesToCatalogV3() {
        String url = "jdbc:h2:mem:catalog_upgrade_" + UUID.randomUUID()
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/migration/common", "classpath:db/migration/h2")
                .target("2")
                .load()
                .migrate();

        MigrateResult upgrade = Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/migration/common", "classpath:db/migration/h2")
                .target("3")
                .load()
                .migrate();

        assertThat(upgrade.migrationsExecuted).isEqualTo(2);
        JdbcTemplate jdbc = new JdbcTemplate(new DriverManagerDataSource(url, "sa", ""));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM buffet_packages", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM soups", Integer.class)).isZero();
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE version = '3' AND success = TRUE",
                Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE version = '6' AND success = TRUE",
                Integer.class)).isEqualTo(1);
    }
}
