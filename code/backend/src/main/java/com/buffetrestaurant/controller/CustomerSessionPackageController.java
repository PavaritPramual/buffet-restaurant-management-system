package com.buffetrestaurant.controller;

import com.buffetrestaurant.dto.response.BuffetPackageResponse;
import com.buffetrestaurant.service.CatalogService;
import com.buffetrestaurant.service.impl.CustomerSessionAccessService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomerSessionPackageController {
    private final CustomerSessionAccessService access;
    private final CatalogService catalog;

    public CustomerSessionPackageController(CustomerSessionAccessService access, CatalogService catalog) {
        this.access = access;
        this.catalog = catalog;
    }

    @GetMapping("/api/v1/dining-sessions/{sessionId}/package")
    @Operation(summary = "Read only the package belonging to the active customer cookie session")
    public ResponseEntity<BuffetPackageResponse> getPackage(@PathVariable Long sessionId,
            @CookieValue(name = CustomerSessionAccessService.COOKIE_NAME, required = false) String credential) {
        var session = access.requireSession(sessionId, credential, false);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(catalog.getPackage(session.packageId()));
    }
}
