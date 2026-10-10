package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.buffetrestaurant.common.UserSessionKeys;
import com.buffetrestaurant.domain.enums.UserRole;
import com.buffetrestaurant.dto.response.UserContext;
import com.buffetrestaurant.service.impl.CustomerSessionAccessService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

/** Real services/transactions; identity fixtures are isolated from login acceptance tests. */
abstract class MenuStockConsumptionScenarios {
    static final long ID = 981901L;
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper json;
    @Autowired CustomerSessionAccessService customerAccess;
    Cookie customer;
    MockHttpSession manager, kitchen;
    @BeforeEach void prepare() {
        cleanup();
        jdbc.update("INSERT INTO buffet_packages(id,name,price,active) VALUES (?,'Recipe Package',299,true)",ID);
        jdbc.update("INSERT INTO soups(id,name,active) VALUES (?,'Recipe Soup',true)",ID);
        jdbc.update("INSERT INTO restaurant_tables(id,table_number,capacity,status) VALUES (?,'RECIPE01',4,'OCCUPIED')",ID);
        jdbc.update("INSERT INTO dining_sessions(id,table_id,package_id,soup_id,adult_count,child_count,package_price_at_open,session_token,status) VALUES (?,?,?,?,2,0,299,'recipe-qr','ACTIVE')",ID,ID,ID,ID);
        jdbc.update("INSERT INTO menu_categories(id,name) VALUES (?,'Recipe Category')",ID);
        jdbc.update("INSERT INTO stock_items(id,sku,name,unit,quantity) VALUES (?,'RECIPE-PORK','หมู','กิโลกรัม',1)",ID);
        jdbc.update("INSERT INTO stock_items(id,sku,name,unit,quantity) VALUES (?,'RECIPE-SAUCE','ซอส','ลิตร',1)",ID+1);
        jdbc.update("INSERT INTO app_users(id,username,password_hash,role,active) VALUES (?,'recipe-kitchen','isolated-identity-fixture','KITCHEN_STAFF',true)",ID);
        customer = new Cookie(CustomerSessionAccessService.COOKIE_NAME,customerAccess.exchange("recipe-qr").credential());
        manager = identity(UserRole.MANAGER); kitchen = identity(UserRole.KITCHEN_STAFF);
    }
    @AfterEach void cleanup() {
        jdbc.update("DELETE FROM stock_transactions WHERE stock_item_id IN (?,?)",ID,ID+1);
        jdbc.update("DELETE FROM orders WHERE session_id=?",ID);
        jdbc.update("DELETE FROM menu_items WHERE category_id=?",ID);
        jdbc.update("DELETE FROM menu_categories WHERE id=?",ID);
        jdbc.update("DELETE FROM dining_sessions WHERE id=?",ID);
        jdbc.update("DELETE FROM restaurant_tables WHERE id=?",ID);
        jdbc.update("DELETE FROM soups WHERE id=?",ID);
        jdbc.update("DELETE FROM buffet_packages WHERE id=?",ID);
        jdbc.update("DELETE FROM stock_items WHERE id IN (?,?)",ID,ID+1);
        jdbc.update("DELETE FROM app_users WHERE id=?",ID);
    }
    MockHttpSession identity(UserRole role) {
        var session = new MockHttpSession();
        session.setAttribute(UserSessionKeys.USER_CONTEXT_SESSION_KEY,new UserContext(ID,"recipe-kitchen","Recipe actor",role));
        return session;
    }
    String menuBody(String name, boolean enabled, String recipe) {
        return "{\"categoryId\":"+ID+",\"name\":\""+name+"\",\"available\":true,\"packageIds\":["+ID+"],\"automaticStockDeduction\":"+enabled+",\"stockUsage\":"+recipe+"}";
    }
    String recipe(String quantity) { return "[{\"stockItemId\":"+ID+",\"quantityPerServing\":"+quantity+"}]"; }
    long create(String name,String recipe) throws Exception {
        var result=mvc.perform(post("/api/v1/menu-items").session(manager).contentType(MediaType.APPLICATION_JSON).content(menuBody(name,true,recipe))).andExpect(status().isCreated()).andReturn();
        return json.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }
    long order(long menu,int quantity) throws Exception {
        var result=mvc.perform(post("/api/v1/dining-sessions/"+ID+"/orders").cookie(customer).header("Origin","http://localhost:5173").contentType(MediaType.APPLICATION_JSON)
                .content("{\"items\":[{\"menuItemId\":"+menu+",\"quantity\":"+quantity+"}]}"))
                .andExpect(status().isCreated()).andReturn();
        return json.readTree(result.getResponse().getContentAsString()).get("orderId").asLong();
    }
    int start(long order) throws Exception {
        return mvc.perform(patch("/api/v1/orders/"+order+"/status").session(kitchen).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"PREPARING\"}")).andReturn().getResponse().getStatus();
    }
    BigDecimal balance() { return jdbc.queryForObject("SELECT quantity FROM stock_items WHERE id=?",BigDecimal.class,ID); }
    int movements() { return jdbc.queryForObject("SELECT count(*) FROM stock_transactions WHERE stock_item_id=?",Integer.class,ID); }

    @Test void snapshotSurvivesRecipeEditAndConsumptionOccursOnlyOnce() throws Exception {
        long menu=create("หมูสไลซ์",recipe("0.100")), order=order(menu,3);
        mvc.perform(put("/api/v1/menu-items/"+menu).session(manager).contentType(MediaType.APPLICATION_JSON).content(menuBody("หมูสไลซ์",false,"[]"))).andExpect(status().isOk());
        assertThat(balance()).isEqualByComparingTo("1");
        assertThat(start(order)).isEqualTo(200);
        assertThat(balance()).isEqualByComparingTo("0.700");
        assertThat(start(order)).isEqualTo(400);
        assertThat(movements()).isEqualTo(1);
        mvc.perform(get("/api/v1/stock/transactions").session(manager)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].transactionType").value("CONSUMPTION")).andExpect(jsonPath("$[0].orderId").value(order))
                .andExpect(jsonPath("$[0].actorUsername").value("recipe-kitchen"));
        long next=order(menu,2); assertThat(start(next)).isEqualTo(200);
        assertThat(balance()).isEqualByComparingTo("0.700");
    }
    @Test void aggregatesSharedIngredientsAndDoesNotLeakRecipeToCustomer() throws Exception {
        long one=create("เมนูหนึ่ง",recipe("0.100")),two=create("เมนูสอง",recipe("0.200"));
        var result=mvc.perform(post("/api/v1/dining-sessions/"+ID+"/orders").cookie(customer).header("Origin","http://localhost:5173").contentType(MediaType.APPLICATION_JSON)
                .content("{\"items\":[{\"menuItemId\":"+one+",\"quantity\":2},{\"menuItemId\":"+two+",\"quantity\":1}]}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.items[0].stockUsage").doesNotExist()).andReturn();
        assertThat(start(json.readTree(result.getResponse().getContentAsString()).get("orderId").asLong())).isEqualTo(200);
        assertThat(balance()).isEqualByComparingTo("0.600"); assertThat(movements()).isEqualTo(1);
        mvc.perform(get("/api/v1/dining-sessions/"+ID+"/menu").cookie(customer)).andExpect(status().isOk()).andExpect(jsonPath("$[0].stockUsage").doesNotExist());
    }
    @Test void insufficientOrInactiveStockRollsBackEntireOrderThenCanRetry() throws Exception {
        String both=recipe("0.300").replace("]",",{\"stockItemId\":"+(ID+1)+",\"quantityPerServing\":0.600}]");
        long menu=create("หมูกับซอส",both),order=order(menu,2);
        assertThat(start(order)).isEqualTo(409); assertThat(balance()).isEqualByComparingTo("1"); assertThat(movements()).isZero();
        assertThat(jdbc.queryForObject("SELECT status FROM orders WHERE id=?",String.class,order)).isEqualTo("RECEIVED");
        jdbc.update("UPDATE stock_items SET quantity=2,active=false WHERE id=?",ID+1);
        assertThat(start(order)).isEqualTo(409); assertThat(movements()).isZero();
        jdbc.update("UPDATE stock_items SET active=true WHERE id=?",ID+1);
        assertThat(start(order)).isEqualTo(200); assertThat(balance()).isEqualByComparingTo("0.400");
    }
    @Test void validatesAtomicWritesAndManagerOnlyRecipeRead() throws Exception {
        for(String invalid:List.of("[]",recipe("0"),recipe("0.0001"),recipe("0.1").replace("]",",{\"stockItemId\":"+ID+",\"quantityPerServing\":0.2}]"))) {
            int status=mvc.perform(post("/api/v1/menu-items").session(manager).contentType(MediaType.APPLICATION_JSON).content(menuBody("Invalid",true,invalid))).andReturn().getResponse().getStatus();
            assertThat(status).isEqualTo(400);
        }
        assertThat(jdbc.queryForObject("SELECT count(*) FROM menu_items WHERE category_id=?",Integer.class,ID)).isZero();
        long menu=create("Valid",recipe("0.1"));
        mvc.perform(get("/api/v1/menu-items/"+menu+"/stock-usage")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/menu-items/"+menu+"/stock-usage").session(kitchen).header("X-User-Role","MANAGER")).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/menu-items/"+menu+"/stock-usage").session(manager)).andExpect(status().isOk()).andExpect(jsonPath("$.stockUsage[0].unit").value("กิโลกรัม"));
        jdbc.update("UPDATE stock_items SET active=false WHERE id=?",ID);
        mvc.perform(put("/api/v1/menu-items/"+menu).session(manager).contentType(MediaType.APPLICATION_JSON).content(menuBody("Changed",true,recipe("0.1"))))
                .andExpect(status().isOk());
        assertThat(jdbc.queryForObject("SELECT name FROM menu_items WHERE id=?",String.class,menu)).isEqualTo("Changed");
        assertThat(jdbc.queryForObject("SELECT quantity_per_serving FROM menu_stock_usage WHERE menu_item_id=?",BigDecimal.class,menu)).isEqualByComparingTo("0.1");
        mvc.perform(put("/api/v1/menu-items/"+menu).session(manager).contentType(MediaType.APPLICATION_JSON).content(menuBody("Changed again",true,recipe("0.2"))))
                .andExpect(status().isConflict());
        assertThat(jdbc.queryForObject("SELECT name FROM menu_items WHERE id=?",String.class,menu)).isEqualTo("Changed");
    }
    @Test void referencesProtectStockUnitAndArchiveInsteadOfHardDelete() throws Exception {
        long menu=create("Referenced",recipe("0.1")); order(menu,1);
        mvc.perform(put("/api/v1/menu-items/"+menu).session(manager).contentType(MediaType.APPLICATION_JSON).content(menuBody("Referenced",false,"[]"))).andExpect(status().isOk());
        mvc.perform(put("/api/v1/stock/items/"+ID).session(manager).contentType(MediaType.APPLICATION_JSON).content("{\"sku\":\"RECIPE-PORK\",\"name\":\"หมู\",\"unit\":\"กรัม\",\"lowStockThreshold\":0}"))
                .andExpect(status().isConflict());
        jdbc.update("UPDATE stock_items SET quantity=0 WHERE id=?",ID);
        mvc.perform(delete("/api/v1/stock/items/"+ID).session(manager)).andExpect(status().isNoContent());
        assertThat(jdbc.queryForObject("SELECT archived_at IS NOT NULL FROM stock_items WHERE id=?",Boolean.class,ID)).isTrue();
    }
    @Test void concurrentStartOfSameOrderConsumesOnlyOnce() throws Exception {
        long order=order(create("Race",recipe("0.3")),1);
        assertThat(race(order,order)).containsExactlyInAnyOrder(200,400);
        assertThat(balance()).isEqualByComparingTo("0.7"); assertThat(movements()).isEqualTo(1);
    }
    @Test void concurrentOrdersCannotOverdrawSharedStock() throws Exception {
        long menu=create("Shared",recipe("0.6")),first=order(menu,1),second=order(menu,1);
        assertThat(race(first,second)).containsExactlyInAnyOrder(200,409);
        assertThat(balance()).isEqualByComparingTo("0.4"); assertThat(movements()).isEqualTo(1);
    }
    List<Integer> race(long first,long second) throws Exception {
        var gate=new CountDownLatch(1); var pool=Executors.newFixedThreadPool(2);
        try {
            var one=pool.submit(() -> { gate.await();return start(first); });
            var two=pool.submit(() -> { gate.await();return start(second); });
            gate.countDown(); return List.of(one.get(15,TimeUnit.SECONDS),two.get(15,TimeUnit.SECONDS));
        } finally { pool.shutdownNow(); }
    }
}
