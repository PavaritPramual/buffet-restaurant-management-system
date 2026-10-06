package com.buffetrestaurant.controller;
import com.buffetrestaurant.config.CustomerOriginGuard;
import com.buffetrestaurant.dto.response.CustomerBillStatusResponse;
import com.buffetrestaurant.service.CustomerBillingService;
import com.buffetrestaurant.service.impl.CustomerSessionAccessService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/dining-sessions/{sessionId}")
public class CustomerBillingController {
    private final CustomerBillingService service;
    private final CustomerOriginGuard origins;
    public CustomerBillingController(CustomerBillingService service, CustomerOriginGuard origins) { this.service = service; this.origins = origins; }
    @GetMapping("/bill-status")
    @Operation(summary = "Read the active customer's calculated bill and payment status")
    public ResponseEntity<CustomerBillStatusResponse> status(@PathVariable Long sessionId,
            @CookieValue(name = CustomerSessionAccessService.COOKIE_NAME, required = false) String credential) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.status(sessionId, credential));
    }
    @PostMapping("/bill-request")
    @Operation(summary = "Request the bill once and stop accepting new orders for this session")
    public ResponseEntity<CustomerBillStatusResponse> request(@PathVariable Long sessionId,
            @CookieValue(name = CustomerSessionAccessService.COOKIE_NAME, required = false) String credential,
            @RequestHeader(name = "Origin", required = false) String origin) {
        origins.requireAllowed(origin);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.request(sessionId, credential));
    }
}
