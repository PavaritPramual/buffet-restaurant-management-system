package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.*;
import java.sql.*;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

class MenuStockMigrationTest {
    @Test void upgradesV18WithoutInventingRecipesForExistingMenusOrOrders() throws Exception {
        String url="jdbc:h2:mem:recipe_upgrade_"+UUID.randomUUID()+";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        Flyway.configure().dataSource(url,"sa","").locations("classpath:db/migration/common","classpath:db/migration/h2").target("18").load().migrate();
        try(var connection=DriverManager.getConnection(url,"sa","");var sql=connection.createStatement()) {
            sql.execute("INSERT INTO menu_categories(id,name) VALUES(1,'Legacy')");
            sql.execute("INSERT INTO menu_items(id,category_id,name,available) VALUES(1,1,'Legacy',true)");
            sql.execute("INSERT INTO stock_items(id,sku,name,unit,quantity) VALUES(1,'LEGACY','Legacy','piece',5)");
            sql.execute("INSERT INTO stock_transactions(stock_item_id,transaction_type,quantity_delta,balance_after,reason) VALUES(1,'IN',5,5,'Legacy')");
        }
        var latest=Flyway.configure().dataSource(url,"sa","").locations("classpath:db/migration/common","classpath:db/migration/h2").load();
        assertThat(latest.migrate().migrationsExecuted).isEqualTo(1); latest.validate();
        try(var connection=DriverManager.getConnection(url,"sa","");var sql=connection.createStatement()) {
            try(var row=sql.executeQuery("SELECT automatic_stock_deduction FROM menu_items WHERE id=1")) { row.next();assertThat(row.getBoolean(1)).isFalse(); }
            try(var row=sql.executeQuery("SELECT COUNT(*) FROM order_item_stock_usage")) { row.next();assertThat(row.getInt(1)).isZero(); }
            try(var row=sql.executeQuery("SELECT order_id FROM stock_transactions")) { row.next();assertThat(row.getObject(1)).isNull(); }
            assertThatThrownBy(()->sql.execute("INSERT INTO menu_stock_usage VALUES(1,1,0,'piece')")).isInstanceOf(SQLException.class);
            sql.execute("INSERT INTO menu_stock_usage VALUES(1,1,0.100,'piece')");
            assertThatThrownBy(()->sql.execute("INSERT INTO menu_stock_usage VALUES(1,1,0.200,'piece')")).isInstanceOf(SQLException.class);
            assertThatThrownBy(()->sql.execute("DELETE FROM stock_items WHERE id=1")).isInstanceOf(SQLException.class);
            assertThatThrownBy(()->sql.execute("INSERT INTO stock_transactions(stock_item_id,transaction_type,quantity_delta,balance_after,reason) VALUES(1,'CONSUMPTION',-1,4,'No order')")).isInstanceOf(SQLException.class);
        }
    }
}
