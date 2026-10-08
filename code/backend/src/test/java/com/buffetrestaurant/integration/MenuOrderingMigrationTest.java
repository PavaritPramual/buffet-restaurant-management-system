package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MenuOrderingMigrationTest {
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    void emptyDatabaseAndFoundationUpgradePreserveMenuOrderingConstraints(int foundationVersion) throws Exception {
        String url = "jdbc:h2:mem:menu_upgrade_" + UUID.randomUUID()
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        if (foundationVersion > 0) {
            Flyway foundation = Flyway.configure().dataSource(url, "sa", "")
                    .locations("classpath:db/migration/common", "classpath:db/migration/h2")
                    .target(MigrationVersion.fromVersion(Integer.toString(foundationVersion))).load();
            foundation.migrate();
            assertThat(foundation.info().current().getVersion().getVersion()).isEqualTo(Integer.toString(foundationVersion));
        }
        Flyway latest = Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/migration/common", "classpath:db/migration/h2")
                .load();
        latest.migrate();
        latest.validate();
        assertThat(latest.info().applied()).extracting(m -> m.getVersion().getVersion())
                .containsExactly("1", "2", "3", "4", "6", "7", "8", "9", "10", "14", "15");
        try (Connection connection = DriverManager.getConnection(url, "sa", "")) {
            assertMenuOrderingSchema(connection);
            assertMenuOrderingConstraints(connection);
        }
    }

    static void assertMenuOrderingSchema(Connection connection) throws Exception {
        DatabaseMetaData metadata = connection.getMetaData();
        assertColumn(metadata, "menu_categories", "name", false, 100);
        assertColumn(metadata, "menu_items", "category_id", false, null);
        assertColumn(metadata, "menu_items", "name", false, 100);
        assertColumn(metadata, "menu_items", "description", true, null);
        assertColumn(metadata, "menu_items", "image_url", true, 500);
        assertColumn(metadata, "menu_items", "available", false, null);
        assertColumn(metadata, "orders", "session_id", false, null);
        assertColumn(metadata, "orders", "table_number", false, 20);
        assertColumn(metadata, "orders", "status", false, 20);
        assertColumn(metadata, "orders", "created_at", false, null);
        assertColumn(metadata, "order_items", "order_id", false, null);
        assertColumn(metadata, "order_items", "menu_item_id", false, null);
        assertColumn(metadata, "order_items", "item_name", false, 100);
        assertColumn(metadata, "order_items", "quantity", false, null);
        assertColumn(metadata, "order_items", "note", true, 255);
        java.util.List<String> primaryKey = new java.util.ArrayList<>();
        try (var keys = metadata.getPrimaryKeys(null, "public", "package_menu_items")) {
            while (keys.next()) primaryKey.add(keys.getString("COLUMN_NAME"));
        }
        assertThat(primaryKey).containsExactlyInAnyOrder("package_id", "menu_item_id");
        assertForeignKey(metadata, "menu_items", "category_id", "menu_categories", DatabaseMetaData.importedKeyRestrict);
        assertForeignKey(metadata, "package_menu_items", "package_id", "buffet_packages", DatabaseMetaData.importedKeyCascade);
        assertForeignKey(metadata, "package_menu_items", "menu_item_id", "menu_items", DatabaseMetaData.importedKeyCascade);
        assertForeignKey(metadata, "orders", "session_id", "dining_sessions", DatabaseMetaData.importedKeyRestrict);
        assertForeignKey(metadata, "order_items", "order_id", "orders", DatabaseMetaData.importedKeyCascade);
        assertForeignKey(metadata, "order_items", "menu_item_id", "menu_items", DatabaseMetaData.importedKeyRestrict);
    }

    private static void assertColumn(DatabaseMetaData metadata, String table, String column, boolean nullable, Integer length) throws Exception {
        try (var result = metadata.getColumns(null, "public", table, column)) {
            assertThat(result.next()).as(table + "." + column).isTrue();
            assertThat(result.getString("IS_NULLABLE")).as(table + "." + column + " nullability").isEqualTo(nullable ? "YES" : "NO");
            if (length != null) assertThat(result.getInt("COLUMN_SIZE")).as(table + "." + column + " length").isEqualTo(length);
        }
    }

    private static void assertForeignKey(DatabaseMetaData metadata, String table, String column, String parent, int deleteRule) throws Exception {
        boolean found = false;
        try (var keys = metadata.getImportedKeys(null, "public", table)) {
            while (keys.next()) {
                if (column.equals(keys.getString("FKCOLUMN_NAME"))) {
                    assertThat(keys.getString("PKTABLE_NAME")).isEqualTo(parent);
                    assertThat(keys.getInt("DELETE_RULE")).isEqualTo(deleteRule);
                    found = true;
                }
            }
        }
        assertThat(found).as("FK " + table + "." + column).isTrue();
    }

    static void assertMenuOrderingConstraints(Connection connection) throws Exception {
        try (var sql = connection.createStatement()) {
            sql.executeUpdate("INSERT INTO buffet_packages(id,name,price,active) VALUES(970001,'Migration package',299,true)");
            sql.executeUpdate("INSERT INTO soups(id,name,active) VALUES(970001,'Migration soup',true)");
            sql.executeUpdate("INSERT INTO restaurant_tables(id,table_number,capacity,status) VALUES(970001,'MIG01',4,'OCCUPIED')");
            sql.executeUpdate("INSERT INTO dining_sessions(id,table_id,package_id,soup_id,adult_count,child_count,package_price_at_open,session_token,status) "
                    + "VALUES(970001,970001,970001,970001,2,0,299,'migration-qr','ACTIVE')");
            sql.executeUpdate("INSERT INTO menu_categories(id,name) VALUES(970001,'Migration category')");
            sql.executeUpdate("INSERT INTO menu_items(id,category_id,name) VALUES(970001,970001,'Migration item')");
            sql.executeUpdate("INSERT INTO package_menu_items(package_id,menu_item_id) VALUES(970001,970001)");
            sql.executeUpdate("INSERT INTO orders(id,session_id,table_number) VALUES(970001,970001,'MIG01')");
            sql.executeUpdate("INSERT INTO order_items(id,order_id,menu_item_id,item_name,quantity) VALUES(970001,970001,970001,'Migration item',1)");
            try (var row = sql.executeQuery("SELECT status,created_at FROM orders WHERE id=970001")) {
                assertThat(row.next()).isTrue();
                assertThat(row.getString("status")).isEqualTo("RECEIVED");
                assertThat(row.getObject("created_at")).isNotNull();
            }
            reject(connection, "UPDATE menu_items SET category_id=999999 WHERE id=970001");
            reject(connection, "INSERT INTO package_menu_items(package_id,menu_item_id) VALUES(999999,970001)");
            reject(connection, "UPDATE orders SET session_id=999999 WHERE id=970001");
            reject(connection, "UPDATE menu_items SET name=NULL WHERE id=970001");
            reject(connection, "UPDATE order_items SET quantity=0 WHERE id=970001");
            reject(connection, "UPDATE order_items SET quantity=-1 WHERE id=970001");
            reject(connection, "DELETE FROM menu_items WHERE id=970001");
            reject(connection, "DELETE FROM dining_sessions WHERE id=970001");
            sql.executeUpdate("DELETE FROM orders WHERE id=970001");
            try (var row = sql.executeQuery("SELECT count(*) FROM order_items WHERE id=970001")) {
                row.next(); assertThat(row.getInt(1)).isZero();
            }
        }
    }

    private static void reject(Connection connection, String statement) {
        SQLException failure = assertThrows(SQLException.class, () -> {
            try (var sql = connection.createStatement()) { sql.executeUpdate(statement); }
        });
        assertThat(failure.getSQLState()).startsWith("23");
    }
}
