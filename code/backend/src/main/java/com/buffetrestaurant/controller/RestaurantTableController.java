package com.buffetrestaurant.controller;

import com.buffetrestaurant.common.ApiPaths;
import com.buffetrestaurant.domain.enums.TableStatus;
import com.buffetrestaurant.dto.request.CreateTableRequest;
import com.buffetrestaurant.dto.request.UpdateTableRequest;
import com.buffetrestaurant.dto.request.UpdateTableStatusRequest;
import com.buffetrestaurant.dto.response.TableResponse;
import com.buffetrestaurant.service.RestaurantTableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/tables")
@Tag(name = "Restaurant Table Management", description = "Operations for managing restaurant tables and viewing table status")
public class RestaurantTableController {

    private final RestaurantTableService tableService;

    public RestaurantTableController(RestaurantTableService tableService) {
        this.tableService = tableService;
    }

    @Operation(summary = "Get all restaurant tables, optionally filtered by status")
    @GetMapping
    public ResponseEntity<List<TableResponse>> getAllTables(
            @RequestParam(name = "status", required = false) TableStatus status
    ) {
        List<TableResponse> tables = tableService.getAllTables(status);
        return ResponseEntity.ok(tables);
    }

    @Operation(summary = "Get a single restaurant table by ID")
    @GetMapping("/{id}")
    public ResponseEntity<TableResponse> getTableById(@PathVariable Long id) {
        TableResponse table = tableService.getTableById(id);
        return ResponseEntity.ok(table);
    }

    @Operation(summary = "Create a new restaurant table")
    @PostMapping
    public ResponseEntity<TableResponse> createTable(@Valid @RequestBody CreateTableRequest request) {
        TableResponse created = tableService.createTable(request);
        URI location = URI.create(ApiPaths.API_V1 + "/tables/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Update an existing restaurant table")
    @PutMapping("/{id}")
    public ResponseEntity<TableResponse> updateTable(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTableRequest request
    ) {
        TableResponse updated = tableService.updateTable(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Update the status of a restaurant table")
    @PatchMapping("/{id}/status")
    public ResponseEntity<TableResponse> updateTableStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTableStatusRequest request
    ) {
        TableResponse updated = tableService.updateTableStatus(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete a restaurant table")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTable(@PathVariable Long id) {
        tableService.deleteTable(id);
        return ResponseEntity.noContent().build();
    }
}
