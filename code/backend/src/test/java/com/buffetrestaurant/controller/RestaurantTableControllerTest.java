package com.buffetrestaurant.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buffetrestaurant.domain.enums.TableStatus;
import com.buffetrestaurant.dto.request.CreateTableRequest;
import com.buffetrestaurant.dto.request.UpdateTableRequest;
import com.buffetrestaurant.dto.request.UpdateTableStatusRequest;
import com.buffetrestaurant.dto.response.TableResponse;
import com.buffetrestaurant.exception.DuplicateResourceException;
import com.buffetrestaurant.exception.GlobalExceptionHandler;
import com.buffetrestaurant.exception.ResourceNotFoundException;
import com.buffetrestaurant.service.RestaurantTableService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RestaurantTableController.class)
@Import(GlobalExceptionHandler.class)
class RestaurantTableControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RestaurantTableService tableService;

    @Test
    void getAllTables_whenCalled_returns200AndTableList() throws Exception {
        TableResponse t1 = new TableResponse(1L, "T01", 4, TableStatus.AVAILABLE);
        TableResponse t2 = new TableResponse(2L, "T02", 2, TableStatus.OCCUPIED);
        when(tableService.getAllTables(null)).thenReturn(List.of(t1, t2));

        mockMvc.perform(get("/api/v1/tables"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].tableNumber").value("T01"))
                .andExpect(jsonPath("$[0].capacity").value(4))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].tableNumber").value("T02"))
                .andExpect(jsonPath("$[1].status").value("OCCUPIED"));
    }

    @Test
    void getAllTables_whenStatusFilterGiven_returns200AndFilteredList() throws Exception {
        TableResponse t1 = new TableResponse(1L, "T01", 4, TableStatus.AVAILABLE);
        when(tableService.getAllTables(TableStatus.AVAILABLE)).thenReturn(List.of(t1));

        mockMvc.perform(get("/api/v1/tables").param("status", "AVAILABLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].tableNumber").value("T01"))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"));
    }

    @Test
    void getTableById_whenTableExists_returns200AndTableResponse() throws Exception {
        TableResponse response = new TableResponse(1L, "T01", 4, TableStatus.AVAILABLE);
        when(tableService.getTableById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/tables/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.tableNumber").value("T01"))
                .andExpect(jsonPath("$.capacity").value(4))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void getTableById_whenTableDoesNotExist_returns404AndErrorResponse() throws Exception {
        when(tableService.getTableById(999L))
                .thenThrow(new ResourceNotFoundException("Restaurant table not found with id: 999"));

        mockMvc.perform(get("/api/v1/tables/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Restaurant table not found with id: 999"))
                .andExpect(jsonPath("$.path").value("/api/v1/tables/999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createTable_whenValid_returns201AndCreatedTable() throws Exception {
        CreateTableRequest request = new CreateTableRequest("T01", 4);
        TableResponse created = new TableResponse(1L, "T01", 4, TableStatus.AVAILABLE);
        when(tableService.createTable(any(CreateTableRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/v1/tables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/tables/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.tableNumber").value("T01"))
                .andExpect(jsonPath("$.capacity").value(4))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void createTable_whenBodyInvalid_returns400AndErrorResponse() throws Exception {
        CreateTableRequest invalid = new CreateTableRequest("", 0);

        mockMvc.perform(post("/api/v1/tables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/tables"));
    }

    @Test
    void createTable_whenDuplicateTableNumber_returns409AndErrorResponse() throws Exception {
        CreateTableRequest request = new CreateTableRequest("T01", 4);
        when(tableService.createTable(any(CreateTableRequest.class)))
                .thenThrow(new DuplicateResourceException("Table number 'T01' already exists"));

        mockMvc.perform(post("/api/v1/tables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Table number 'T01' already exists"))
                .andExpect(jsonPath("$.path").value("/api/v1/tables"));
    }

    @Test
    void updateTable_whenValid_returns200AndUpdatedTable() throws Exception {
        UpdateTableRequest request = new UpdateTableRequest("T01-UPDATED", 6);
        TableResponse updated = new TableResponse(1L, "T01-UPDATED", 6, TableStatus.AVAILABLE);
        when(tableService.updateTable(eq(1L), any(UpdateTableRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/tables/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.tableNumber").value("T01-UPDATED"))
                .andExpect(jsonPath("$.capacity").value(6));
    }

    @Test
    void updateTable_whenDuplicateTableNumber_returns409AndErrorResponse() throws Exception {
        UpdateTableRequest request = new UpdateTableRequest("T02", 4);
        when(tableService.updateTable(eq(1L), any(UpdateTableRequest.class)))
                .thenThrow(new DuplicateResourceException("Table number 'T02' is already in use by another table"));

        mockMvc.perform(put("/api/v1/tables/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Table number 'T02' is already in use by another table"))
                .andExpect(jsonPath("$.path").value("/api/v1/tables/1"));
    }

    @Test
    void updateTable_whenTableNotFound_returns404AndErrorResponse() throws Exception {
        UpdateTableRequest request = new UpdateTableRequest("T99", 4);
        when(tableService.updateTable(eq(999L), any(UpdateTableRequest.class)))
                .thenThrow(new ResourceNotFoundException("Restaurant table not found with id: 999"));

        mockMvc.perform(put("/api/v1/tables/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value("/api/v1/tables/999"));
    }

    @Test
    void updateTableStatus_whenValid_returns200AndUpdatedTable() throws Exception {
        UpdateTableStatusRequest request = new UpdateTableStatusRequest(TableStatus.OCCUPIED);
        TableResponse updated = new TableResponse(1L, "T01", 4, TableStatus.OCCUPIED);
        when(tableService.updateTableStatus(eq(1L), any(UpdateTableStatusRequest.class))).thenReturn(updated);

        mockMvc.perform(patch("/api/v1/tables/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("OCCUPIED"));
    }

    @Test
    void updateTableStatus_whenUnsupportedEnum_returns400AndErrorResponse() throws Exception {
        mockMvc.perform(patch("/api/v1/tables/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"occupied\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Malformed request or unsupported enum value"))
                .andExpect(jsonPath("$.path").value("/api/v1/tables/1/status"));
    }

    @Test
    void deleteTable_whenSuccess_returns204NoContent() throws Exception {
        doNothing().when(tableService).deleteTable(1L);

        mockMvc.perform(delete("/api/v1/tables/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTable_whenTableOccupied_returns400AndErrorResponse() throws Exception {
        doThrow(new IllegalStateException("Cannot delete table while it is occupied"))
                .when(tableService).deleteTable(1L);

        mockMvc.perform(delete("/api/v1/tables/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Cannot delete table while it is occupied"))
                .andExpect(jsonPath("$.path").value("/api/v1/tables/1"));
    }

    @Test
    void deleteTable_whenTableNotFound_returns404AndErrorResponse() throws Exception {
        doThrow(new ResourceNotFoundException("Restaurant table not found with id: 999"))
                .when(tableService).deleteTable(999L);

        mockMvc.perform(delete("/api/v1/tables/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value("/api/v1/tables/999"));
    }
}
