package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.buffetrestaurant.exception.GlobalExceptionHandler;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Real PostgreSQL upgrade/constraints; HTTP probe is test-only, not an application auth test. */
@Testcontainers(disabledWithoutDocker = true)
class PostgresMasterDataArchiveMigrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("buffet_test_archive").withUsername("postgres").withPassword("backend-test")
            .withInitScript("postgres-test-roles.sql");

    @Test void legacyDefaultsChecksAndRealDatabaseErrorsUseConflictResponse() throws Exception {
        Flyway.configure().dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration/common", "classpath:db/migration/postgresql")
                .target("15").load().migrate();
        JdbcTemplate jdbc = new JdbcTemplate(new DriverManagerDataSource(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword()));
        jdbc.update("INSERT INTO restaurant_tables(id,table_number,capacity,status) VALUES (1,'LEGACY',4,'AVAILABLE')");
        jdbc.update("INSERT INTO buffet_packages(id,name,price,active) VALUES (1,'Legacy',299,true)");
        jdbc.update("INSERT INTO soups(id,name,active) VALUES (1,'Legacy',true)");
        Flyway latest = Flyway.configure().dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration/common", "classpath:db/migration/postgresql").load();
        latest.migrate(); latest.validate();
        assertThat(latest.info().applied()).extracting(m -> m.getVersion().getVersion()).contains("17");
        assertThat(latest.info().pending()).isEmpty();

        var mvc = standaloneSetup(new DatabaseConstraintProbe(jdbc))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        for (String resource : new String[]{"table", "package", "soup"}) {
            mvc.perform(post("/test/r01/check-" + resource)).andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409)).andExpect(jsonPath("$.error").value("Conflict"))
                    .andExpect(jsonPath("$.path").value("/test/r01/check-" + resource))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(result -> assertThat(result.getResolvedException())
                            .isInstanceOf(DataIntegrityViolationException.class)
                            .hasRootCauseInstanceOf(org.postgresql.util.PSQLException.class));
            mvc.perform(post("/test/r01/check-" + resource)).andExpect(result -> {
                var cause = ((DataIntegrityViolationException) result.getResolvedException()).getMostSpecificCause();
                assertThat(((java.sql.SQLException) cause).getSQLState()).isEqualTo("23514");
            });
        }
        for (String table : new String[]{"restaurant_tables", "buffet_packages", "soups"}) {
            assertThat(jdbc.queryForObject("SELECT archived FROM " + table + " WHERE id=1", Boolean.class)).isFalse();
        }
        assertThat(jdbc.queryForObject("SELECT status FROM restaurant_tables WHERE id=1", String.class)).isEqualTo("AVAILABLE");
        jdbc.execute("CREATE TABLE r01_test_reference (table_id BIGINT REFERENCES restaurant_tables(id) ON DELETE RESTRICT)");
        jdbc.update("INSERT INTO r01_test_reference VALUES (1)");
        mvc.perform(post("/test/r01/fk")).andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409)).andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(DataIntegrityViolationException.class)
                        .hasRootCauseInstanceOf(org.postgresql.util.PSQLException.class));
        mvc.perform(post("/test/r01/fk")).andExpect(result -> {
            var cause = ((DataIntegrityViolationException) result.getResolvedException()).getMostSpecificCause();
            assertThat(((java.sql.SQLException) cause).getSQLState()).isEqualTo("23503");
        });
        assertThat(jdbc.queryForObject("SELECT count(*) FROM restaurant_tables WHERE id=1", Integer.class)).isOne();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM r01_test_reference", Integer.class)).isOne();

        jdbc.update("UPDATE restaurant_tables SET archived=true WHERE id=1");
        jdbc.update("UPDATE buffet_packages SET active=false,archived=true WHERE id=1");
        jdbc.update("UPDATE soups SET active=false,archived=true WHERE id=1");
        for (String table : new String[]{"restaurant_tables", "buffet_packages", "soups"}) {
            assertThat(jdbc.queryForObject("SELECT archived FROM " + table + " WHERE id=1", Boolean.class)).isTrue();
        }
    }

    @RestController
    static class DatabaseConstraintProbe {
        private final JdbcTemplate jdbc;
        DatabaseConstraintProbe(JdbcTemplate jdbc) { this.jdbc = jdbc; }
        @PostMapping("/test/r01/check-table")
        void invalidTable() { jdbc.update("UPDATE restaurant_tables SET archived=true,status='OCCUPIED' WHERE id=1"); }
        @PostMapping("/test/r01/check-package")
        void invalidPackage() { jdbc.update("UPDATE buffet_packages SET archived=true WHERE id=1"); }
        @PostMapping("/test/r01/check-soup")
        void invalidSoup() { jdbc.update("UPDATE soups SET archived=true WHERE id=1"); }
        @PostMapping("/test/r01/fk")
        void invalidDelete() { jdbc.update("DELETE FROM restaurant_tables WHERE id=1"); }
    }
}
