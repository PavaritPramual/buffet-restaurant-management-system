package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.*;
import java.sql.DriverManager;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

class MasterDataArchiveMigrationTest {
    @Test void upgradeFromV15KeepsLegacyRowsAndAppliesSafeDefaultsAndConstraints() throws Exception {
        String url="jdbc:h2:mem:archive_"+UUID.randomUUID()+";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        Flyway.configure().dataSource(url,"sa","").locations("classpath:db/migration/common","classpath:db/migration/h2").target("15").load().migrate();
        try(var connection=DriverManager.getConnection(url,"sa","");var sql=connection.createStatement()) {
            sql.execute("INSERT INTO restaurant_tables(id,table_number,capacity,status) VALUES (1,'LEGACY',4,'AVAILABLE')");
            sql.execute("INSERT INTO buffet_packages(id,name,price,active) VALUES (1,'Legacy',299,true)");
            sql.execute("INSERT INTO soups(id,name,active) VALUES (1,'Legacy',true)");
            Flyway flyway=Flyway.configure().dataSource(url,"sa","").locations("classpath:db/migration/common","classpath:db/migration/h2").load();
            flyway.migrate(); flyway.validate();
            assertThat(flyway.info().applied()).extracting(m -> m.getVersion().getVersion()).contains("17");
            assertThat(flyway.info().pending()).isEmpty();
            for(String table:new String[]{"restaurant_tables","buffet_packages","soups"}) {
                try(var rows=sql.executeQuery("SELECT archived FROM "+table+" WHERE id=1")) {
                    assertThat(rows.next()).isTrue(); assertThat(rows.getBoolean(1)).isFalse();
                }
            }
            assertThatThrownBy(() -> sql.execute("UPDATE buffet_packages SET archived=true WHERE id=1")).isInstanceOf(java.sql.SQLException.class);
            assertThatThrownBy(() -> sql.execute("UPDATE soups SET archived=true WHERE id=1")).isInstanceOf(java.sql.SQLException.class);
            assertThatThrownBy(() -> sql.execute("UPDATE restaurant_tables SET archived=true,status='OCCUPIED' WHERE id=1")).isInstanceOf(java.sql.SQLException.class);
        }
    }
}
