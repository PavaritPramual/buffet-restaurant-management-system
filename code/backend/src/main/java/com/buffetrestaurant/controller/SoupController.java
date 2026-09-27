package com.buffetrestaurant.controller;

import com.buffetrestaurant.common.ApiPaths;
import com.buffetrestaurant.dto.request.ActiveStatusRequest;
import com.buffetrestaurant.dto.request.SoupRequest;
import com.buffetrestaurant.dto.response.SoupResponse;
import com.buffetrestaurant.service.CatalogService;
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
@RequestMapping(ApiPaths.API_V1 + "/soups")
@Tag(name = "Soup Management")
public class SoupController {
    private final CatalogService service;

    public SoupController(CatalogService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List soups, optionally filtered by active status")
    public List<SoupResponse> list(@RequestParam(required = false) Boolean active) {
        return service.getSoups(active);
    }

    @GetMapping("/{id}")
    public SoupResponse get(@PathVariable Long id) {
        return service.getSoup(id);
    }

    @PostMapping
    public ResponseEntity<SoupResponse> create(@Valid @RequestBody SoupRequest request) {
        SoupResponse created = service.createSoup(request);
        return ResponseEntity.created(URI.create(ApiPaths.API_V1 + "/soups/" + created.id()))
                .body(created);
    }

    @PutMapping("/{id}")
    public SoupResponse update(@PathVariable Long id, @Valid @RequestBody SoupRequest request) {
        return service.updateSoup(id, request);
    }

    @PatchMapping("/{id}/active")
    public SoupResponse setActive(@PathVariable Long id,
                                  @Valid @RequestBody ActiveStatusRequest request) {
        return service.setSoupActive(id, request.active());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.disableSoup(id);
        return ResponseEntity.noContent().build();
    }
}