package com.buffetrestaurant.controller;

import com.buffetrestaurant.dto.response.BuffetPackageResponse;
import com.buffetrestaurant.service.CustomerSessionPackageService;
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
    private final CustomerSessionPackageService packages;

    public CustomerSessionPackageController(CustomerSessionPackageService packages) {
        this.packages = packages;
    }

    @GetMapping("/api/v1/dining-sessions/{sessionId}/package")
    @Operation(summary = "Read only the package belonging to the active customer cookie session",
            description = "The existing session may read its assigned package even if archived or inactive. "
                    + "This does not expose archived packages for selection in new sessions.")
    public ResponseEntity<BuffetPackageResponse> getPackage(@PathVariable Long sessionId,
            @CookieValue(name = CustomerSessionAccessService.COOKIE_NAME, required = false) String credential) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(packages.getPackage(sessionId, credential));
    }
}
