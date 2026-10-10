package com.buffetrestaurant.integration;

import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.*;

@SpringBootTest(properties={"spring.config.import=", "app.ordering.session-provider=database", "app.menu.admin-access-provider=session", "app.fulfillment.access-provider=session"})
@AutoConfigureMockMvc @ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named="MENU_TEST_PG_URL",matches=".+")
@EnabledIfEnvironmentVariable(named="ALLOW_DESTRUCTIVE_DB_TESTS",matches="true")
class PostgresMenuStockConsumptionIntegrationTest extends MenuStockConsumptionScenarios {
    @org.junit.jupiter.api.Test void recipeTablesDenyClientRolesAndHaveRlsAndReverseIndexes() {
        for(String table:java.util.List.of("menu_stock_usage","order_item_stock_usage")) {
            org.assertj.core.api.Assertions.assertThat(jdbc.queryForObject("SELECT relrowsecurity FROM pg_class WHERE oid=?::regclass",Boolean.class,"public."+table)).isTrue();
            for(String role:java.util.List.of("anon","authenticated")) for(String privilege:java.util.List.of("SELECT","INSERT","UPDATE","DELETE")) {
                org.assertj.core.api.Assertions.assertThat(jdbc.queryForObject("SELECT has_table_privilege(?, ?, ?)",Boolean.class,role,"public."+table,privilege)).isFalse();
            }
            org.assertj.core.api.Assertions.assertThat(jdbc.queryForObject("SELECT count(*) FROM pg_indexes WHERE schemaname='public' AND tablename=? AND indexdef LIKE '%(stock_item_id)%'",Integer.class,table)).isGreaterThan(0);
        }
    }
    @DynamicPropertySource static void postgres(DynamicPropertyRegistry registry) {
        String url=DisposablePostgresDatabase.requireReady("MENU_TEST");
        registry.add("spring.datasource.url",()->url);
        registry.add("spring.datasource.driver-class-name",()->"org.postgresql.Driver");
        registry.add("spring.datasource.username",()->System.getenv().getOrDefault("MENU_TEST_PG_USER","postgres"));
        registry.add("spring.datasource.password",()->System.getenv().getOrDefault("MENU_TEST_PG_PASSWORD",""));
        registry.add("spring.flyway.locations",()->"classpath:db/migration/common,classpath:db/migration/postgresql");
    }
}
