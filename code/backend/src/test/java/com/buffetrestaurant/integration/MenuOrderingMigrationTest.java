package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;

class MenuOrderingMigrationTest {
    private static final String URL =
            "jdbc:h2:mem:menu_ordering_upgrade;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";

    @Test
    void migrate_whenDatabaseAlreadyHasV1AndV2_appliesV3Successfully() {
        Flyway throughV2 = Flyway.configure()
                .dataSource(URL, "sa", "")
                .locations("classpath:db/migration/common")
                .target(MigrationVersion.fromVersion("2"))
                .load();
        throughV2.migrate();
        assertThat(throughV2.info().current().getVersion().getVersion()).isEqualTo("2");

        Flyway latest = Flyway.configure()
                .dataSource(URL, "sa", "")
                .locations("classpath:db/migration/common")
                .load();
        latest.migrate();

        assertThat(latest.info().current().getVersion().getVersion()).isEqualTo("3");
    }
}
