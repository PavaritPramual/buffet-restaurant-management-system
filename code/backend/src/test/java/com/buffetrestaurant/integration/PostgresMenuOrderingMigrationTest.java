package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.DriverManager;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/** Requires explicit opt-in and a marked, fresh disposable loopback database. */
@EnabledIfEnvironmentVariable(named = "MENU_TEST_PG_URL", matches = ".+")
@EnabledIfEnvironmentVariable(named = "ALLOW_DESTRUCTIVE_DB_TESTS", matches = "true")
class PostgresMenuOrderingMigrationTest {
    @Test
    void foundationV2UpgradesAndEnforcesCatalogConstraintsAndClientRoleRestrictions() throws Exception {
        String url = DisposablePostgresDatabase.requireReady("MENU_TEST");
        String user = System.getenv().getOrDefault("MENU_TEST_PG_USER", "postgres");
        String password = System.getenv().getOrDefault("MENU_TEST_PG_PASSWORD", "");
        Flyway foundation = Flyway.configure().dataSource(url, user, password)
                .locations("classpath:db/migration/common", "classpath:db/migration/postgresql")
                .target(MigrationVersion.fromVersion("2")).load();
        foundation.migrate();
        assertThat(foundation.info().current().getVersion().getVersion()).isEqualTo("2");
        Flyway latest = Flyway.configure().dataSource(url, user, password)
                .locations("classpath:db/migration/common", "classpath:db/migration/postgresql").load();
        latest.migrate(); latest.validate();
        assertThat(latest.info().applied()).extracting(m -> m.getVersion().getVersion())
                .containsSubsequence("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17");
        assertThat(latest.info().pending()).isEmpty();
        try (var connection = DriverManager.getConnection(url, user, password); var sql = connection.createStatement()) {
            MenuOrderingMigrationTest.assertMenuOrderingSchema(connection);
            MenuOrderingMigrationTest.assertMenuOrderingConstraints(connection);
            for (String table : new String[]{"menu_categories", "menu_items", "package_menu_items", "orders", "order_items"}) {
                try (var row = sql.executeQuery("SELECT relrowsecurity FROM pg_class WHERE oid='public." + table + "'::regclass")) {
                    row.next(); assertThat(row.getBoolean(1)).as(table + " RLS").isTrue();
                }
                for (String role : new String[]{"anon", "authenticated"}) {
                    for (String privilege : new String[]{"SELECT", "INSERT", "UPDATE", "DELETE"}) {
                        try (var row = sql.executeQuery("SELECT has_table_privilege('" + role + "','public." + table + "','" + privilege + "')")) {
                            row.next(); assertThat(row.getBoolean(1)).as(role + " " + table + " " + privilege).isFalse();
                        }
                    }
                }
            }
            for (String sequence : new String[]{"menu_categories_id_seq", "menu_items_id_seq", "orders_id_seq", "order_items_id_seq"}) {
                for (String role : new String[]{"anon", "authenticated"}) {
                    try (var row = sql.executeQuery("SELECT has_sequence_privilege('" + role + "','public." + sequence + "','USAGE')")) {
                        row.next(); assertThat(row.getBoolean(1)).isFalse();
                    }
                }
            }
        }
    }
}
