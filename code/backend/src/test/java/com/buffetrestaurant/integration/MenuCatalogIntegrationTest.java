package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buffetrestaurant.domain.BuffetPackage;
import com.buffetrestaurant.domain.MenuCategory;
import com.buffetrestaurant.domain.MenuItem;
import com.buffetrestaurant.domain.enums.UserRole;
import com.buffetrestaurant.dto.request.CreateUserRequest;
import com.buffetrestaurant.repository.BuffetPackageRepository;
import com.buffetrestaurant.repository.MenuCategoryRepository;
import com.buffetrestaurant.repository.MenuItemRepository;
import com.buffetrestaurant.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

/** Exercises the real Auth session provider, HTTP validation and persisted catalog. */
@SpringBootTest(properties = "app.menu.admin-access-provider=session")
@AutoConfigureMockMvc
@Transactional
class MenuCatalogIntegrationTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;
    @Autowired private AuthService auth;
    @Autowired private BuffetPackageRepository packages;
    @Autowired private MenuCategoryRepository categories;
    @Autowired private MenuItemRepository items;
    @Autowired private EntityManager entities;

    private BuffetPackage buffetPackage;
    private MenuCategory category;
    private MenuItem item;

    @BeforeEach
    void seedCatalog() {
        buffetPackage = packages.save(new BuffetPackage("Step 2 package", new BigDecimal("299"), null));
        category = categories.save(new MenuCategory("Step 2 category"));
        item = items.saveAndFlush(new MenuItem(category, "Step 2 item", true, null, Set.of(buffetPackage.getId())));
    }

    @Test
    void managerCanCreateReadUpdateAndDeleteCategory() throws Exception {
        MockHttpSession manager = login(UserRole.MANAGER);
        String body = mvc.perform(post("/api/v1/menu-categories").session(manager)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"  เครื่องดื่ม  \"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.name").value("เครื่องดื่ม"))
                .andReturn().getResponse().getContentAsString();
        long id = json.readTree(body).get("id").asLong();
        mvc.perform(get("/api/v1/menu-categories/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("เครื่องดื่ม"));
        mvc.perform(put("/api/v1/menu-categories/" + id).session(manager)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"ชาและกาแฟ\"}"))
                .andExpect(status().isOk());
        entities.flush(); entities.clear();
        assertThat(categories.findById(id).orElseThrow().getName()).isEqualTo("ชาและกาแฟ");
        mvc.perform(get("/api/v1/menu-categories")).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + id + ")].name").value(org.hamcrest.Matchers.hasItem("ชาและกาแฟ")));
        mvc.perform(delete("/api/v1/menu-categories/" + id).session(manager)).andExpect(status().isNoContent());
        entities.flush(); entities.clear();
        assertThat(categories.findById(id)).isEmpty();
        mvc.perform(get("/api/v1/menu-categories/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void managerCanCreateReadUpdateAndDeleteItemIncludingPackageLinksAndOptionalFields() throws Exception {
        MockHttpSession manager = login(UserRole.MANAGER);
        String body = mvc.perform(post("/api/v1/menu-items").session(manager)
                        .contentType(MediaType.APPLICATION_JSON).content(itemJson(category.getId(), buffetPackage.getId(), "  ชาไทย  ", true)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.name").value("ชาไทย"))
                .andReturn().getResponse().getContentAsString();
        long id = json.readTree(body).get("id").asLong();
        mvc.perform(get("/api/v1/menu-items/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Fresh tea"))
                .andExpect(jsonPath("$.imageUrl").value("/images/tea.jpg"));
        MenuCategory updatedCategory = categories.save(new MenuCategory("New category"));
        BuffetPackage updatedPackage = packages.save(new BuffetPackage("New package", new BigDecimal("399"), null));
        mvc.perform(put("/api/v1/menu-items/" + id).session(manager).contentType(MediaType.APPLICATION_JSON)
                        .content(itemJson(updatedCategory.getId(), updatedPackage.getId(), "ชาไทยเย็น", false)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.packageIds[0]").value(updatedPackage.getId().intValue()));
        entities.flush(); entities.clear();
        MenuItem persisted = items.findById(id).orElseThrow();
        assertThat(persisted.getName()).isEqualTo("ชาไทยเย็น");
        assertThat(persisted.getCategory().getId()).isEqualTo(updatedCategory.getId());
        assertThat(persisted.getPackageIds()).containsExactly(updatedPackage.getId());
        assertThat(persisted.getDescription()).isEqualTo("Fresh tea");
        assertThat(persisted.isAvailable()).isFalse();
        mvc.perform(delete("/api/v1/menu-items/" + id).session(manager)).andExpect(status().isNoContent());
        entities.flush(); entities.clear();
        assertThat(items.findById(id)).isEmpty();
        mvc.perform(get("/api/v1/menu-items/" + id)).andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class, names = {"SERVICE_STAFF", "KITCHEN_STAFF", "SUPERVISOR"})
    void otherStaffRolesCannotPerformAnyCatalogMutation(UserRole role) throws Exception {
        assertAllMutationsRejected(login(role), 403);
    }

    @Test
    void anonymousCustomerCannotPerformAnyCatalogMutation() throws Exception {
        assertAllMutationsRejected(null, 401);
    }

    private void assertAllMutationsRejected(MockHttpSession session, int expectedStatus) throws Exception {
        MockHttpServletRequestBuilder[] requests = {
                post("/api/v1/menu-categories").content("{\"name\":\"Blocked\"}"),
                put("/api/v1/menu-categories/" + category.getId()).content("{\"name\":\"Blocked\"}"),
                delete("/api/v1/menu-categories/" + category.getId()),
                post("/api/v1/menu-items").content(itemJson(category.getId(), buffetPackage.getId(), "Blocked", true)),
                put("/api/v1/menu-items/" + item.getId()).content(itemJson(category.getId(), buffetPackage.getId(), "Blocked", true)),
                delete("/api/v1/menu-items/" + item.getId())
        };
        for (MockHttpServletRequestBuilder request : requests) {
            if (session != null) request.session(session);
            mvc.perform(request.contentType(MediaType.APPLICATION_JSON)).andExpect(status().is(expectedStatus))
                    .andExpect(jsonPath("$.status").value(expectedStatus));
        }
        entities.clear();
        assertThat(categories.findById(category.getId()).orElseThrow().getName()).isEqualTo("Step 2 category");
        assertThat(items.findById(item.getId()).orElseThrow().getName()).isEqualTo("Step 2 item");
    }

    @Test
    void duplicateCategoryAndInvalidItemUpdatesLeavePersistedDataUnchanged() throws Exception {
        MockHttpSession manager = login(UserRole.MANAGER);
        categories.saveAndFlush(new MenuCategory("Existing category"));
        mvc.perform(put("/api/v1/menu-categories/" + category.getId()).session(manager)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"EXISTING CATEGORY\"}"))
                .andExpect(status().isConflict());
        mvc.perform(put("/api/v1/menu-items/" + item.getId()).session(manager).contentType(MediaType.APPLICATION_JSON)
                        .content(itemJson(category.getId(), 999999L, "Invalid replacement", false)))
                .andExpect(status().isBadRequest());
        entities.clear();
        assertThat(categories.findById(category.getId()).orElseThrow().getName()).isEqualTo("Step 2 category");
        MenuItem persisted = items.findById(item.getId()).orElseThrow();
        assertThat(persisted.getName()).isEqualTo("Step 2 item");
        assertThat(persisted.isAvailable()).isTrue();
        assertThat(persisted.getPackageIds()).containsExactly(buffetPackage.getId());
    }

    @Test
    void categoryWithItemsCannotBeDeletedAndValidationUsesSharedErrorResponse() throws Exception {
        MockHttpSession manager = login(UserRole.MANAGER);
        mvc.perform(delete("/api/v1/menu-categories/" + category.getId()).session(manager))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/menu-categories").session(manager).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\" \"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.path").value("/api/v1/menu-categories"))
                .andExpect(jsonPath("$.status").value(400));
        mvc.perform(post("/api/v1/menu-items").session(manager).contentType(MediaType.APPLICATION_JSON)
                        .content(itemJson(category.getId(), buffetPackage.getId(), "x".repeat(101), true)))
                .andExpect(status().isBadRequest());
        assertThat(categories.findById(category.getId())).isPresent();
    }

    @Test
    void menuPaginationSupportsBothDirectionsNextPageAndEmptyPage() throws Exception {
        items.save(new MenuItem(category, "Alpha", true, null, Set.of(buffetPackage.getId())));
        items.saveAndFlush(new MenuItem(category, "Zulu", false, null, Set.of(buffetPackage.getId())));
        mvc.perform(get("/api/v1/menu-items").param("page", "0").param("size", "1").param("sort", "name,asc"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].name").value("Alpha"))
                .andExpect(jsonPath("$.totalElements").value(3)).andExpect(jsonPath("$.totalPages").value(3));
        mvc.perform(get("/api/v1/menu-items").param("page", "0").param("size", "1").param("sort", "name,desc"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].name").value("Zulu"));
        mvc.perform(get("/api/v1/menu-items").param("page", "1").param("size", "1").param("sort", "name,asc"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].name").value("Step 2 item"));
        mvc.perform(get("/api/v1/menu-items").param("page", "9").param("size", "1").param("sort", "id,asc"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content.length()").value(0));
        mvc.perform(get("/api/v1/menu-items").param("sort", "available,asc"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].available").value(false));
    }

    @ParameterizedTest
    @CsvSource(value = {"-1|10|id,asc", "0|0|id,asc", "0|101|id,asc", "0|10|password,asc",
            "0|10|name,sideways", "0|10|name,asc,extra", "0|10|name,"}, delimiter = '|')
    void invalidPaginationAndSortReturn400(int page, int size, String sort) throws Exception {
        mvc.perform(get("/api/v1/menu-items").param("page", Integer.toString(page))
                        .param("size", Integer.toString(size)).param("sort", sort))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
    }

    private MockHttpSession login(UserRole role) throws Exception {
        String username = "menu-" + UUID.randomUUID();
        auth.createUser(new CreateUserRequest(username, "password123", "Catalog test", null, role,
                "Catalog", "Tester", null));
        return (MockHttpSession) mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"password123\"}"))
                .andExpect(status().isOk()).andReturn().getRequest().getSession(false);
    }

    private String itemJson(long categoryId, long packageId, String name, boolean available) {
        return """
                {"categoryId":%d,"name":"%s","description":" Fresh tea ","available":%s,
                 "packageIds":[%d],"imageUrl":"/images/tea.jpg"}
                """.formatted(categoryId, name, available, packageId);
    }
}
