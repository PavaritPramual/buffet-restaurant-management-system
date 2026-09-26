package com.buffetrestaurant.controller;

import com.buffetrestaurant.common.ApiPaths;
import com.buffetrestaurant.dto.request.MenuCategoryRequest;
import com.buffetrestaurant.dto.response.MenuCategoryResponse;
import com.buffetrestaurant.service.MenuCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/menu-categories")
@Tag(name = "Menu Category Management")
public class MenuCategoryController {
    private final MenuCatalogService service;
    public MenuCategoryController(MenuCatalogService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "List menu categories")
    public List<MenuCategoryResponse> list() { return service.getCategories(); }

    @GetMapping("/{id}")
    public MenuCategoryResponse get(@PathVariable Long id) { return service.getCategory(id); }

    @PostMapping
    public ResponseEntity<MenuCategoryResponse> create(@Valid @RequestBody MenuCategoryRequest request) {
        MenuCategoryResponse created = service.createCategory(request);
        return ResponseEntity.created(URI.create(ApiPaths.API_V1 + "/menu-categories/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public MenuCategoryResponse update(@PathVariable Long id, @Valid @RequestBody MenuCategoryRequest request) {
        return service.updateCategory(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
