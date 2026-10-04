package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class DisposablePostgresDatabaseTest {
    private static final String URL = "jdbc:postgresql://127.0.0.1:55432/buffet_test_menu";

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"false", "TRUE", "1", " true "})
    void requiresExactExplicitOptIn(String flag) {
        var environment = new HashMap<String, String>();
        environment.put("MENU_TEST_PG_URL", URL);
        if (flag != null) environment.put("ALLOW_DESTRUCTIVE_DB_TESTS", flag);
        assertThatIllegalStateException().isThrownBy(() ->
                DisposablePostgresDatabase.requireUrl("MENU_TEST", environment));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "", "jdbc:postgresql://team.supabase.co:5432/buffet_test_menu",
            "jdbc:postgresql://127.0.0.1:55432/postgres",
            "jdbc:postgresql://127.0.0.1:55432/buffet",
            "jdbc:postgresql://127.0.0.1/buffet_test_menu",
            "jdbc:postgresql://127.0.0.1:0/buffet_test_menu",
            "jdbc:postgresql://127.0.0.1:65536/buffet_test_menu",
            "jdbc:postgresql://127.0.0.1:55432/buffet_test_menu?options=-csearch_path=team",
            "jdbc:postgresql://127.0.0.1:55432/buffet_test_menu?host=team.supabase.co",
            "jdbc:postgresql://127.0.0.1:55432,team:5432/buffet_test_menu",
            "jdbc:postgresql:buffet_test_menu"
    })
    void rejectsUnsafeOrAmbiguousTargetsBeforeConnection(String url) {
        assertThatIllegalStateException().isThrownBy(() -> DisposablePostgresDatabase.requireUrl(
                "MENU_TEST", Map.of("ALLOW_DESTRUCTIVE_DB_TESTS", "true", "MENU_TEST_PG_URL", url)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"127.0.0.1", "localhost", "[::1]"})
    void allowsOnlyExplicitLocalTestTargets(String host) {
        String url = "jdbc:postgresql://" + host + ":55432/buffet_test_menu";
        assertThat(DisposablePostgresDatabase.requireUrl("MENU_TEST", Map.of(
                "ALLOW_DESTRUCTIVE_DB_TESTS", "true", "MENU_TEST_PG_URL", url))).isEqualTo(url);
    }

    @Test
    void refusesMissingUrlAndOverlongDatabaseName() {
        assertThatIllegalStateException().isThrownBy(() -> DisposablePostgresDatabase.requireUrl(
                "DINING_TEST", Map.of("ALLOW_DESTRUCTIVE_DB_TESTS", "true")));
        assertThatIllegalStateException().isThrownBy(() -> DisposablePostgresDatabase.requireUrl(
                "MENU_TEST", Map.of("ALLOW_DESTRUCTIVE_DB_TESTS", "true",
                        "MENU_TEST_PG_URL", URL + "x".repeat(64))));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"team database", "buffet-disposable-test-only "})
    void refusesAnUnmarkedDatabase(String comment) throws Exception {
        Connection connection = database("buffet_test_menu", comment);
        assertThatIllegalStateException().isThrownBy(() ->
                DisposablePostgresDatabase.verifyDatabase(connection, URL));
    }

    @Test
    void refusesMismatchedActualDatabaseEvenWhenMarked() throws Exception {
        Connection connection = database("buffet_test_other", DisposablePostgresDatabase.MARKER);
        assertThatIllegalStateException().isThrownBy(() ->
                DisposablePostgresDatabase.verifyDatabase(connection, URL));
    }

    @Test
    void acceptsMatchingMarkedDatabase() throws Exception {
        DisposablePostgresDatabase.verifyDatabase(database("buffet_test_menu", DisposablePostgresDatabase.MARKER), URL);
    }

    private Connection database(String name, String comment) throws Exception {
        Connection connection = mock(Connection.class);
        Statement sql = mock(Statement.class);
        ResultSet row = mock(ResultSet.class);
        when(connection.createStatement()).thenReturn(sql);
        when(sql.executeQuery("SELECT current_database(), shobj_description(oid, 'pg_database') "
                + "FROM pg_database WHERE datname = current_database()")).thenReturn(row);
        when(row.next()).thenReturn(true);
        when(row.getString(1)).thenReturn(name);
        when(row.getString(2)).thenReturn(comment);
        return connection;
    }
}
