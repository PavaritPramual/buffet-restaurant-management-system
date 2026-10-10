package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.buffetrestaurant.domain.*;
import com.buffetrestaurant.repository.*;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

/** Committed fixture, HTTP service transaction and rollback; no outer test transaction. */
@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:r01_fk_rollback;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "app.menu.admin-access-provider=fixture"})
@AutoConfigureMockMvc
class MenuArchiveFkRollbackTest {
    @Autowired JdbcTemplate jdbc;
    @Autowired TransactionTemplate transactions;
    @Autowired MockMvc mvc;
    @Autowired BuffetPackageRepository packages;
    @Autowired MenuCategoryRepository categories;
    @Autowired MenuItemRepository items;

    @Test void anUnexpectedFkRollsBackTheEntireHardDeleteWithoutArchivingOrDetachingData() throws Exception {
        // Models a reference introduced outside the service's early existence check.
        jdbc.execute("CREATE TABLE r01_external_reference (menu_item_id BIGINT REFERENCES menu_items(id) ON DELETE RESTRICT)");
        long[] ids = transactions.execute(status -> {
            var pack = packages.save(new BuffetPackage("FK rollback package",BigDecimal.TEN,null));
            var category = categories.save(new MenuCategory("FK rollback category"));
            var item = items.saveAndFlush(new MenuItem(category,"FK rollback item",true,null,Set.of()));
            jdbc.update("INSERT INTO r01_external_reference(menu_item_id) VALUES (?)", item.getId());
            return new long[]{pack.getId(),category.getId(),item.getId()};
        });
        try {
            mvc.perform(delete("/api/v1/menu-items/"+ids[2])).andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409)).andExpect(jsonPath("$.path").value("/api/v1/menu-items/"+ids[2]));
            assertThat(jdbc.queryForObject("SELECT count(*) FROM menu_items WHERE id=? AND archived_at IS NULL AND available=true",Integer.class,ids[2])).isOne();
            assertThat(jdbc.queryForObject("SELECT count(*) FROM menu_categories WHERE id=? AND archived_at IS NULL",Integer.class,ids[1])).isOne();
            assertThat(jdbc.queryForObject("SELECT count(*) FROM r01_external_reference WHERE menu_item_id=?",Integer.class,ids[2])).isOne();
        } finally {
            jdbc.execute("DROP TABLE r01_external_reference");
            transactions.executeWithoutResult(status -> { items.deleteById(ids[2]); items.flush(); categories.deleteById(ids[1]); packages.deleteById(ids[0]); });
        }
    }
}
