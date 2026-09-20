package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buffetrestaurant.domain.RestaurantTable;
import com.buffetrestaurant.domain.enums.TableStatus;
import com.buffetrestaurant.dto.request.CreateTableRequest;
import com.buffetrestaurant.dto.request.UpdateTableStatusRequest;
import com.buffetrestaurant.repository.RestaurantTableRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
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
class RestaurantTableIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestaurantTableRepository tableRepository;

    @BeforeEach
    void setUp() {
        tableRepository.deleteAll();
    }

    @Test
    void contextLoads_whenApplicationStarts_contextLoadsSuccessfully() {
        assertThat(tableRepository).isNotNull();
    }

    @Test
    void createTable_whenValid_persistsAndReturns201() throws Exception {
        CreateTableRequest request = new CreateTableRequest("INT-01", 4);

        mockMvc.perform(post("/api/v1/tables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.tableNumber").value("INT-01"))
                .andExpect(jsonPath("$.capacity").value(4))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));

        Optional<RestaurantTable> persisted = tableRepository.findByTableNumber("INT-01");
        assertThat(persisted).isPresent();
        assertThat(persisted.get().getCapacity()).isEqualTo(4);
        assertThat(persisted.get().getStatus()).isEqualTo(TableStatus.AVAILABLE);
    }

    @Test
    void getTableById_whenTableExists_returns200AndTable() throws Exception {
        RestaurantTable saved = tableRepository.save(new RestaurantTable("INT-02", 6));

        mockMvc.perform(get("/api/v1/tables/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.tableNumber").value("INT-02"))
                .andExpect(jsonPath("$.capacity").value(6))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void updateTableStatus_whenValid_persistsNewStatus() throws Exception {
        RestaurantTable saved = tableRepository.save(new RestaurantTable("INT-03", 2));
        UpdateTableStatusRequest request = new UpdateTableStatusRequest(TableStatus.OCCUPIED);

        mockMvc.perform(patch("/api/v1/tables/" + saved.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.status").value("OCCUPIED"));

        RestaurantTable updated = tableRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(TableStatus.OCCUPIED);
    }

    @Test
    void deleteTable_whenAvailable_removesFromPersistence() throws Exception {
        RestaurantTable saved = tableRepository.save(new RestaurantTable("INT-04", 4));

        mockMvc.perform(delete("/api/v1/tables/" + saved.getId()))
                .andExpect(status().isNoContent());

        assertThat(tableRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void createTable_whenDuplicateTableNumber_returns409Conflict() throws Exception {
        tableRepository.save(new RestaurantTable("INT-DUP", 4));
        CreateTableRequest duplicateRequest = new CreateTableRequest("INT-DUP", 2);

        mockMvc.perform(post("/api/v1/tables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Table number 'INT-DUP' already exists"))
                .andExpect(jsonPath("$.path").value("/api/v1/tables"));
    }
}
