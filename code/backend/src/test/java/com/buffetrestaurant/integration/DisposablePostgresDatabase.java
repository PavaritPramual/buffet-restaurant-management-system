package com.buffetrestaurant.integration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.regex.Pattern;

/** Fail closed before connecting, migrating or deleting in environment-driven PostgreSQL tests. */
final class DisposablePostgresDatabase {
    static final String MARKER = "buffet-disposable-test-only";
    private static final Pattern LOCAL_TEST_URL = Pattern.compile(
            "jdbc:postgresql://(?:127\\.0\\.0\\.1|localhost|\\[::1\\]):([0-9]{1,5})/(buffet_test_[a-z0-9_]+)");

    private DisposablePostgresDatabase() {}

    static String requireUrl(String prefix, Map<String, String> environment) {
        if (!"true".equals(environment.get("ALLOW_DESTRUCTIVE_DB_TESTS"))) {
            throw new IllegalStateException("Destructive PostgreSQL tests require ALLOW_DESTRUCTIVE_DB_TESTS=true");
        }
        String url = environment.getOrDefault(prefix + "_PG_URL", "");
        var match = LOCAL_TEST_URL.matcher(url);
        if (!match.matches() || Integer.parseInt(match.group(1)) < 1
                || Integer.parseInt(match.group(1)) > 65535 || match.group(2).length() > 63) {
            throw new IllegalStateException(prefix + "_PG_URL must use an explicit loopback port and a buffet_test_ database; URL options are forbidden");
        }
        return url;
    }

    static String requireReady(String prefix) {
        var environment = System.getenv();
        String url = requireUrl(prefix, environment);
        try (var connection = DriverManager.getConnection(url,
                environment.getOrDefault(prefix + "_PG_USER", "postgres"),
                environment.getOrDefault(prefix + "_PG_PASSWORD", ""))) {
            connection.setReadOnly(true);
            verifyDatabase(connection, url);
        } catch (SQLException exception) {
            throw new IllegalStateException("Cannot verify disposable PostgreSQL database before test startup", exception);
        }
        return url;
    }

    static void verifyDatabase(Connection connection, String url) throws SQLException {
        String expectedDatabase = url.substring(url.lastIndexOf('/') + 1);
        try (var sql = connection.createStatement(); var row = sql.executeQuery(
                "SELECT current_database(), shobj_description(oid, 'pg_database') "
                + "FROM pg_database WHERE datname = current_database()")) {
            if (!row.next() || !expectedDatabase.equals(row.getString(1)) || !MARKER.equals(row.getString(2))) {
                throw new IllegalStateException("Refusing PostgreSQL test: database name or disposable database comment does not match");
            }
        }
    }
}
