package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.assertThat;
import java.sql.DriverManager;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.flywaydb.core.api.MigrationVersion;

/** Legacy V15 data upgraded through the allocated V18 migration and validated. */
class MenuArchiveMigrationTest {
    @Test void upgradePreservesLegacyDataReferencesAndAvailabilityAndCreatesIndexes() throws Exception {
        String url="jdbc:h2:mem:r01_delta_"+UUID.randomUUID()+";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        Flyway baseline=Flyway.configure().dataSource(url,"sa","")
                .locations("classpath:db/migration/common","classpath:db/migration/h2")
                .target(MigrationVersion.fromVersion("15")).load();
        baseline.migrate();
        try (var connection=DriverManager.getConnection(url,"sa",""); var sql=connection.createStatement()) {
            sql.execute("INSERT INTO buffet_packages(id,name,price,active) VALUES (1,'legacy package',299,true)");
            sql.execute("INSERT INTO soups(id,name,active) VALUES (1,'legacy soup',true)");
            sql.execute("INSERT INTO restaurant_tables(id,table_number,capacity,status) VALUES (1,'LEGACY',4,'AVAILABLE')");
            sql.execute("INSERT INTO dining_sessions(id,table_id,package_id,soup_id,adult_count,child_count,package_price_at_open,session_token,status) VALUES (1,1,1,1,2,0,299,'legacy-qr','COMPLETED')");
            sql.execute("INSERT INTO menu_categories(id,name) VALUES (1,'legacy category')");
            sql.execute("INSERT INTO menu_items(id,category_id,name,available) VALUES (1,1,'legacy item',true)");
            sql.execute("INSERT INTO package_menu_items(package_id,menu_item_id) VALUES (1,1)");
            sql.execute("INSERT INTO orders(id,session_id,table_number) VALUES (1,1,'LEGACY')");
            sql.execute("INSERT INTO order_items(order_id,menu_item_id,item_name,quantity) VALUES (1,1,'legacy snapshot',2)");
        }
        Flyway latest=Flyway.configure().dataSource(url,"sa","")
                .locations("classpath:db/migration/common","classpath:db/migration/h2").load();
        latest.migrate(); latest.validate();
        assertThat(latest.info().current().getVersion().getVersion()).isEqualTo("18");
        try (var connection=DriverManager.getConnection(url,"sa",""); var sql=connection.createStatement()) {
            try (var rows=sql.executeQuery("SELECT i.available,i.archived_at,c.archived_at,o.item_name,o.quantity FROM menu_items i JOIN menu_categories c ON c.id=i.category_id JOIN order_items o ON o.menu_item_id=i.id")) {
                assertThat(rows.next()).isTrue(); assertThat(rows.getBoolean(1)).isTrue();
                assertThat(rows.getObject(2)).isNull(); assertThat(rows.getObject(3)).isNull();
                assertThat(rows.getString(4)).isEqualTo("legacy snapshot"); assertThat(rows.getInt(5)).isEqualTo(2);
            }
            MenuOrderingMigrationTest.assertMenuOrderingSchema(connection); // FK rules unchanged
            for (String table:new String[]{"menu_items","menu_categories"}) {
                boolean indexed=false;
                try (var indexes=connection.getMetaData().getIndexInfo(null,"public",table,false,false)) {
                    while (indexes.next()) if ("archived_at".equals(indexes.getString("COLUMN_NAME"))) indexed=true;
                }
                assertThat(indexed).as(table+" archive index").isTrue();
            }
        }
    }
}
