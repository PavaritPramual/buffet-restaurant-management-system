package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buffetrestaurant.domain.BuffetPackage;
import com.buffetrestaurant.domain.Soup;
import com.buffetrestaurant.repository.BuffetPackageRepository;
import com.buffetrestaurant.repository.SoupRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CatalogIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BuffetPackageRepository packageRepository;

    @Autowired
    private SoupRepository soupRepository;

    @BeforeEach
    void clearCatalog() {
        packageRepository.deleteAll();
        soupRepository.deleteAll();
    }

    @Test
    void createPackagePersistsPriceAndReturnsLocation() throws Exception {
        mockMvc.perform(post("/api/v1/buffet-packages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Premium","price":499.00,"description":"Premium buffet"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Premium"))
                .andExpect(jsonPath("$.active").value(true));

        BuffetPackage saved = packageRepository.findAll().get(0);
        assertThat(saved.getPrice()).isEqualByComparingTo(new BigDecimal("499.00"));
        assertThat(saved.getDescription()).isEqualTo("Premium buffet");
    }

    @Test
    void rejectInvalidPackagePriceWithSharedErrorShape() throws Exception {
        mockMvc.perform(post("/api/v1/buffet-packages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Invalid","price":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/v1/buffet-packages"));
        assertThat(packageRepository.count()).isZero();
    }

    @Test
    void updateAndDisablePackagePreservesHistoricalRow() throws Exception {
        BuffetPackage saved = packageRepository.save(new BuffetPackage("Standard", new BigDecimal("299.00"), null));
        mockMvc.perform(put("/api/v1/buffet-packages/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Standard Plus","price":329.00,"description":"Updated"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Standard Plus"));

        mockMvc.perform(delete("/api/v1/buffet-packages/" + saved.getId()))
                .andExpect(status().isNoContent());
        assertThat(packageRepository.findById(saved.getId())).isPresent()
                .get().extracting(BuffetPackage::isActive).isEqualTo(false);

        mockMvc.perform(get("/api/v1/buffet-packages").param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void soupCanBeDisabledAndRestoredForSelection() throws Exception {
        Soup soup = soupRepository.save(new Soup("Tom Yum"));
        mockMvc.perform(delete("/api/v1/soups/" + soup.getId()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/soups").param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(patch("/api/v1/soups/" + soup.getId() + "/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(get("/api/v1/soups").param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Tom Yum"));
    }

    @Test
    void createAndUpdateSoupThroughApi() throws Exception {
        mockMvc.perform(post("/api/v1/soups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Clear Broth\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Clear Broth"))
                .andExpect(jsonPath("$.active").value(true));

        Soup saved = soupRepository.findAll().get(0);
        mockMvc.perform(put("/api/v1/soups/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Tom Yum\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tom Yum"));
        assertThat(soupRepository.findById(saved.getId()).orElseThrow().getName()).isEqualTo("Tom Yum");
    }

    @Test
    void soupRequiresNameAndMissingResourceReturns404() throws Exception {
        mockMvc.perform(post("/api/v1/soups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        mockMvc.perform(get("/api/v1/soups/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}