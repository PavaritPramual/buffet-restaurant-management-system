package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;

class MenuOrderingMigrationTest {
    private static final String URL =
            "jdbc:h2:mem:menu_ordering_upgrade;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";

    @Test
    void migrate_whenDatabaseAlreadyHasPackageCatalogV3_appliesMenuOrderingV4Successfully() {
        Flyway throughV3 = Flyway.configure()
                .dataSource(URL, "sa", "")
                .locations("classpath:db/migration/common", "classpath:db/migration/h2")
                .target(MigrationVersion.fromVersion("3"))
                .load();
        throughV3.migrate();
        assertThat(throughV3.info().current().getVersion().getVersion()).isEqualTo("3");

        Flyway latest = Flyway.configure()
                .dataSource(URL, "sa", "")
                .locations("classpath:db/migration/common", "classpath:db/migration/h2")
                .load();
        latest.migrate();

        assertThat(latest.info().current().getVersion().getVersion()).isEqualTo("4");
    }
}
