package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.buffetrestaurant.domain.*;
import com.buffetrestaurant.domain.enums.UserRole;
import com.buffetrestaurant.dto.request.CreateUserRequest;
import com.buffetrestaurant.exception.*;
import com.buffetrestaurant.repository.*;
import com.buffetrestaurant.service.*;
import com.buffetrestaurant.service.impl.CustomerSessionAccessService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** Real persisted H2, Auth cookies, customer QR exchange and allocated V18 migration. */
@SpringBootTest(properties = {"app.menu.admin-access-provider=session", "app.ordering.session-provider=database",
        "spring.datasource.url=jdbc:h2:mem:r01_menu_archive;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1"})
@AutoConfigureMockMvc
@Transactional
class MenuArchiveIntegrationTest {
    private static final long SESSION = 963001L;
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired AuthService auth;
    @Autowired MenuCatalogService catalog;
    @Autowired MenuCategoryRepository categories;
    @Autowired MenuItemRepository items;
    @Autowired CustomerOrderRepository orders;
    @Autowired CustomerSessionAccessService customers;
    @Autowired JdbcTemplate jdbc;
    @Autowired EntityManager entities;
    MenuCategory category;
    MenuItem item;
    Cookie customer;
    MockHttpSession manager;
    CustomerOrder historical;

    @BeforeEach void seed() throws Exception {
        jdbc.update("INSERT INTO buffet_packages(id,name,price,active) VALUES (963001,'R01 Package',299,true)");
        jdbc.update("INSERT INTO soups(id,name,active) VALUES (963001,'R01 Soup',true)");
        jdbc.update("INSERT INTO restaurant_tables(id,table_number,capacity,status) VALUES (963001,'R01',4,'OCCUPIED')");
        jdbc.update("INSERT INTO dining_sessions(id,table_id,package_id,soup_id,adult_count,child_count,package_price_at_open,session_token,status) "
                + "VALUES (963001,963001,963001,963001,2,0,299,'r01-test-qr','ACTIVE')");
        category = categories.save(new MenuCategory("R01 หมวดอาหาร"));
        item = items.saveAndFlush(new MenuItem(category, "อาหารชื่อเดิม", true, null, Set.of(SESSION)));
        historical = new CustomerOrder(SESSION, "R01");
        historical.addItem(item.getId(), "ชื่อ snapshot ตอนสั่ง", 2);
        historical = orders.saveAndFlush(historical);
        customer = new Cookie("customer_session", customers.exchange("r01-test-qr").credential());
        manager = login(UserRole.MANAGER);
    }

    @Test void historyRemovalIsIdempotentAndPreservesReferencesSnapshotsAndPackageLinks() throws Exception {
        remove(item.getId());
        entities.refresh(item); // compare stored precision, rather than pre-flush Java nanoseconds
        var archivedAt = items.findById(item.getId()).orElseThrow().getArchivedAt();
        remove(item.getId());
        entities.flush(); entities.clear();
        MenuItem archived = items.findById(item.getId()).orElseThrow();
        assertThat(archived.getArchivedAt()).isEqualTo(archivedAt);
        assertThat(archived.isAvailable()).isFalse();
        assertThat(archived.getPackageIds()).containsExactly(SESSION);
        assertThat(orders.findById(historical.getId()).orElseThrow().getItems()).singleElement()
                .satisfies(line -> { assertThat(line.getItemName()).isEqualTo("ชื่อ snapshot ตอนสั่ง");
                    assertThat(line.getMenuItemId()).isEqualTo(item.getId()); assertThat(line.getQuantity()).isEqualTo(2); });
        mvc.perform(get("/api/v1/menu-items/" + item.getId())).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/menu-items")).andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(get("/api/v1/menu-items/archived").session(manager))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(item.getId()));
        mvc.perform(get("/api/v1/dining-sessions/" + SESSION + "/orders/" + historical.getId()).cookie(customer))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items[0].name").value("ชื่อ snapshot ตอนสั่ง"));
        assertThat(jdbc.queryForObject("SELECT package_price_at_open FROM dining_sessions WHERE id=963001", Integer.class)).isEqualTo(299);
    }

    @Test void unusedItemAndEmptyCategoryArePhysicallyDeletedAndRetryIs404() throws Exception {
        MenuCategory empty = categories.save(new MenuCategory("unused"));
        MenuItem unused = items.saveAndFlush(new MenuItem(empty, "unused", true, null, Set.of()));
        remove(unused.getId());
        assertThat(items.existsById(unused.getId())).isFalse();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM package_menu_items WHERE menu_item_id=?", Integer.class, unused.getId())).isZero();
        mvc.perform(delete("/api/v1/menu-items/" + unused.getId()).session(manager)).andExpect(status().isNotFound());
        mvc.perform(delete("/api/v1/menu-categories/" + empty.getId()).session(manager)).andExpect(status().isNoContent());
        assertThat(categories.existsById(empty.getId())).isFalse();
        mvc.perform(delete("/api/v1/menu-categories/" + empty.getId()).session(manager)).andExpect(status().isNotFound());
        mvc.perform(post("/api/v1/menu-items/" + unused.getId() + "/restore").session(manager)).andExpect(status().isNotFound());
    }

    @Test void categoryBlocksUnavailableWorkingChildThenArchivesWithoutDeletingArchivedChildren() throws Exception {
        item.setAvailable(false); items.flush();
        mvc.perform(delete("/api/v1/menu-categories/" + category.getId()).session(manager))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("หมวดหมู่นี้ยังมีเมนูในรายการใช้งาน กรุณาย้ายหรือเก็บเมนูออกก่อน"));
        assertThat(category.isArchived()).isFalse();
        remove(item.getId());
        mvc.perform(delete("/api/v1/menu-categories/" + category.getId()).session(manager)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/v1/menu-categories/" + category.getId()).session(manager)).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/menu-categories")).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/v1/menu-categories/archived").session(manager)).andExpect(jsonPath("$[0].id").value(category.getId()));
        assertThat(items.findById(item.getId()).orElseThrow().getCategory().getId()).isEqualTo(category.getId());
        mvc.perform(post("/api/v1/menu-items/" + item.getId() + "/restore").session(manager)).andExpect(status().isConflict());
        mvc.perform(post("/api/v1/menu-categories/" + category.getId() + "/restore").session(manager)).andExpect(status().isOk());
        assertThat(item.isArchived()).isTrue();
        mvc.perform(post("/api/v1/menu-categories/" + category.getId() + "/restore").session(manager)).andExpect(status().isOk());
        assertThat(item.isArchived()).isTrue();
    }

    @Test void unusedItemWithPackageMembershipArchivesAndPreservesItsConfigurationLink() throws Exception {
        MenuItem unused = items.saveAndFlush(new MenuItem(category,"configured unused",true,null,Set.of(SESSION)));
        remove(unused.getId());
        assertThat(unused.isArchived()).isTrue();
        assertThat(unused.getPackageIds()).containsExactly(SESSION);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM package_menu_items WHERE menu_item_id=?",Integer.class,unused.getId())).isOne();
    }

    @Test void restoreStartsUnavailableAndRepeatedRestorePreservesLaterActivation() throws Exception {
        remove(item.getId());
        mvc.perform(post("/api/v1/menu-items/" + item.getId() + "/restore").session(manager))
                .andExpect(status().isOk()).andExpect(jsonPath("$.available").value(false));
        mvc.perform(put("/api/v1/menu-items/" + item.getId()).session(manager).contentType(MediaType.APPLICATION_JSON)
                .content(input(category.getId(), "เปิดขายอีกครั้ง", true))).andExpect(status().isOk());
        mvc.perform(post("/api/v1/menu-items/" + item.getId() + "/restore").session(manager))
                .andExpect(status().isOk()).andExpect(jsonPath("$.available").value(true));
        mvc.perform(get("/api/v1/dining-sessions/" + SESSION + "/menu").cookie(customer)).andExpect(jsonPath("$[0].name").value("เปิดขายอีกครั้ง"));
        assertThat(orders.findById(historical.getId()).orElseThrow().getItems().get(0).getItemName()).isEqualTo("ชื่อ snapshot ตอนสั่ง");
    }

    @Test void qrCustomerStaleCartIsRejectedAtomicallyAndCannotReactivateArchivedItem() throws Exception {
        mvc.perform(get("/api/v1/dining-sessions/" + SESSION + "/menu").cookie(customer)).andExpect(jsonPath("$[0].id").value(item.getId()));
        remove(item.getId());
        MenuItem good = items.saveAndFlush(new MenuItem(category, "ยังขาย", true, null, Set.of(SESSION)));
        mvc.perform(get("/api/v1/dining-sessions/" + SESSION + "/menu").cookie(customer)).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(post("/api/v1/dining-sessions/" + SESSION + "/orders").cookie(customer)
                        .header("Origin", "http://localhost:5173").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"menuItemId\":" + good.getId() + ",\"quantity\":1},{\"menuItemId\":" + item.getId() + ",\"quantity\":1}]}"))
                .andExpect(status().isConflict());
        assertThat(orders.count()).isOne();
        mvc.perform(put("/api/v1/menu-items/" + item.getId()).session(manager).contentType(MediaType.APPLICATION_JSON)
                .content(input(category.getId(), "forged activation", true))).andExpect(status().isConflict());
        assertThat(item.isAvailable()).isFalse();
    }

    @Test void archivedNamesStayReservedAndArchivedParentCannotReceiveNewOrMovedItems() throws Exception {
        remove(item.getId());
        mvc.perform(delete("/api/v1/menu-categories/" + category.getId()).session(manager)).andExpect(status().isNoContent());
        mvc.perform(post("/api/v1/menu-categories").session(manager).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"R01 หมวดอาหาร\"}")).andExpect(status().isConflict());
        mvc.perform(post("/api/v1/menu-items").session(manager).contentType(MediaType.APPLICATION_JSON)
                .content(input(category.getId(), "อาหารใหม่", true))).andExpect(status().isConflict());
        MenuCategory other = categories.save(new MenuCategory("other"));
        MenuItem moving = items.saveAndFlush(new MenuItem(other, "moving", true, null, Set.of(SESSION)));
        mvc.perform(put("/api/v1/menu-items/" + moving.getId()).session(manager).contentType(MediaType.APPLICATION_JSON)
                .content(input(category.getId(), "moving", true))).andExpect(status().isConflict());
        assertThat(moving.getCategory().getId()).isEqualTo(other.getId());
        mvc.perform(post("/api/v1/menu-items").session(manager).contentType(MediaType.APPLICATION_JSON)
                .content(input(other.getId(), "อาหารชื่อเดิม", true))).andExpect(status().isConflict());
    }

    @Test void thirtyItemsHaveSeparateAccurateMainAndArchivePages() throws Exception {
        for (int i=1; i<30; i++) {
            MenuItem next = items.saveAndFlush(new MenuItem(category, "อาหาร " + i, true, null, Set.of(SESSION)));
            if (i <= 14) { CustomerOrder history = new CustomerOrder(SESSION, "R01"); history.addItem(next.getId(), next.getName(), 1); orders.saveAndFlush(history); remove(next.getId()); }
        }
        remove(item.getId());
        mvc.perform(get("/api/v1/menu-items").param("size", "10")).andExpect(jsonPath("$.totalElements").value(15)).andExpect(jsonPath("$.totalPages").value(2));
        mvc.perform(get("/api/v1/menu-items/archived").session(manager).param("page", "1").param("size", "10").param("sort", "name,desc"))
                .andExpect(jsonPath("$.totalElements").value(15)).andExpect(jsonPath("$.content.length()").value(5));
        mvc.perform(get("/api/v1/menu-items/archived").session(manager).param("sort", "password,asc")).andExpect(status().isBadRequest());
    }

    @ParameterizedTest @EnumSource(value=UserRole.class, names={"SUPERVISOR", "SERVICE_STAFF", "KITCHEN_STAFF"})
    void nonManagersCannotReadOrMutateArchiveEvenWithForgedRoleHeader(UserRole role) throws Exception {
        rejectAll(login(role), 403);
    }
    @Test void anonymousCannotReadOrMutateArchive() throws Exception { rejectAll(null, 401); }
    private void rejectAll(MockHttpSession session, int status) throws Exception {
        for (String resource : new String[]{"menu-items", "menu-categories"}) {
            long id = resource.equals("menu-items") ? item.getId() : category.getId();
            for (var request : new org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder[]{
                    get("/api/v1/" + resource + "/archived"), post("/api/v1/" + resource + "/" + id + "/restore"), delete("/api/v1/" + resource + "/" + id)}) {
                if (session != null) request.session(session);
                mvc.perform(request.header("X-User-Role", "MANAGER")).andExpect(status().is(status));
            }
        }
        assertThat(item.isArchived()).isFalse(); assertThat(category.isArchived()).isFalse();
    }

    @Test void directServiceCallsEnforceAuthorizationWithoutController() {
        RequestContextHolder.resetRequestAttributes();
        try {
            assertThatThrownBy(() -> catalog.getArchivedCategories()).isInstanceOf(AuthenticationRequiredException.class);
            assertThatThrownBy(() -> catalog.getArchivedMenuItems(0,10,"id,asc")).isInstanceOf(AuthenticationRequiredException.class);
            assertThatThrownBy(() -> catalog.restoreMenuItem(item.getId())).isInstanceOf(AuthenticationRequiredException.class);
            assertThatThrownBy(() -> catalog.restoreCategory(category.getId())).isInstanceOf(AuthenticationRequiredException.class);
            assertThatThrownBy(() -> catalog.deleteMenuItem(item.getId())).isInstanceOf(AuthenticationRequiredException.class);
        } finally { RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest())); }
    }

    private void remove(long id) throws Exception { mvc.perform(delete("/api/v1/menu-items/" + id).session(manager)).andExpect(status().isNoContent()); }
    private String input(long categoryId, String name, boolean available) {
        return "{\"categoryId\":" + categoryId + ",\"name\":\"" + name + "\",\"available\":" + available + ",\"packageIds\":[963001]}";
    }
    private MockHttpSession login(UserRole role) throws Exception {
        String username = "r01-" + UUID.randomUUID();
        auth.createUser(new CreateUserRequest(username,"password123","R01 tester",null,role,"R01","Tester",null));
        return (MockHttpSession) mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"password\":\"password123\"}")).andExpect(status().isOk()).andReturn().getRequest().getSession(false);
    }
}
