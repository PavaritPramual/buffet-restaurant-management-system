package com.buffetrestaurant.controller;

import com.buffetrestaurant.common.ApiPaths;
import com.buffetrestaurant.dto.request.ActiveStatusRequest;
import com.buffetrestaurant.dto.request.BuffetPackageRequest;
import com.buffetrestaurant.dto.response.BuffetPackageResponse;
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
@RequestMapping(ApiPaths.API_V1 + "/buffet-packages")
@Tag(name = "Buffet Package Management")
public class BuffetPackageController {
    private final CatalogService service;

    public BuffetPackageController(CatalogService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List packages, optionally filtered by active status")
    public List<BuffetPackageResponse> list(@RequestParam(required = false) Boolean active) {
        return service.getPackages(active);
    }

    @GetMapping("/{id}")
    public BuffetPackageResponse get(@PathVariable Long id) {
        return service.getPackage(id);
    }

    @PostMapping
    public ResponseEntity<BuffetPackageResponse> create(@Valid @RequestBody BuffetPackageRequest request) {
        BuffetPackageResponse created = service.createPackage(request);
        return ResponseEntity.created(URI.create(ApiPaths.API_V1 + "/buffet-packages/" + created.id()))
                .body(created);
    }

    @PutMapping("/{id}")
    public BuffetPackageResponse update(@PathVariable Long id,
                                        @Valid @RequestBody BuffetPackageRequest request) {
        return service.updatePackage(id, request);
    }

    @PatchMapping("/{id}/active")
    public BuffetPackageResponse setActive(@PathVariable Long id,
                                           @Valid @RequestBody ActiveStatusRequest request) {
        return service.setPackageActive(id, request.active());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.disablePackage(id);
        return ResponseEntity.noContent().build();
    }
}