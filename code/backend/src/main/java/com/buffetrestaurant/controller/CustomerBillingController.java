package com.buffetrestaurant.controller;
import com.buffetrestaurant.config.CustomerOriginGuard;
import com.buffetrestaurant.dto.response.CustomerBillStatusResponse;
import com.buffetrestaurant.service.CustomerBillingService;
import com.buffetrestaurant.service.impl.CustomerSessionAccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/dining-sessions/{sessionId}")
@Tag(name = "Customer Bill")
@SecurityRequirement(name = "customerSessionCookie")
public class CustomerBillingController {
    private final CustomerBillingService service;
    private final CustomerOriginGuard origins;
    public CustomerBillingController(CustomerBillingService service, CustomerOriginGuard origins) { this.service = service; this.origins = origins; }
    @GetMapping("/bill-status")
    @Operation(summary = "Read the active customer's calculated bill and payment status",
            description = "Authenticates with the HttpOnly customer_session cookie. The response is not cacheable. "
                    + "totalAmount is the net bill total; dueAmount and paidAmount are separate numeric amounts.")
    @ApiResponse(responseCode = "200", description = "Bill status without QR credentials or payment internals",
            content = @Content(schema = @Schema(implementation = CustomerBillStatusResponse.class),
                    examples = @ExampleObject(name = "paidBill", value = "{\"sessionId\":12,\"status\":\"PAID\","
                            + "\"requestedAt\":\"2026-10-07T08:00:00Z\",\"bill\":{\"sessionId\":12,"
                            + "\"subtotalAmount\":997.50,\"discountAmount\":0.00,\"totalAmount\":997.50},"
                            + "\"dueAmount\":0.00,\"paidAmount\":997.50}")))
    @ApiResponse(responseCode = "401", description = "Customer cookie is missing, invalid or expired")
    @ApiResponse(responseCode = "404", description = "Customer cookie does not authorize this active session")
    public ResponseEntity<CustomerBillStatusResponse> status(@PathVariable Long sessionId,
            @CookieValue(name = CustomerSessionAccessService.COOKIE_NAME, required = false) String credential) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.status(sessionId, credential));
    }
    @PostMapping("/bill-request")
    @Operation(summary = "Request the bill once and stop accepting new orders for this session",
            description = "Uses the HttpOnly customer_session cookie and an allowed Origin header. "
                    + "This request does not record payment or close the dining session. "
                    + "No real cookie or QR credential is included in the example.")
    @ApiResponse(responseCode = "200", description = "Bill is requested and the updated bill status is returned",
            content = @Content(schema = @Schema(implementation = CustomerBillStatusResponse.class),
                    examples = @ExampleObject(name = "requestedBill", value = "{\"sessionId\":12,\"status\":\"REQUESTED\","
                            + "\"requestedAt\":\"2026-10-07T08:00:00Z\",\"bill\":{\"sessionId\":12,"
                            + "\"subtotalAmount\":997.50,\"discountAmount\":0.00,\"totalAmount\":997.50},"
                            + "\"dueAmount\":997.50,\"paidAmount\":0.00}")))
    @ApiResponse(responseCode = "401", description = "Customer cookie is missing, invalid or expired")
    @ApiResponse(responseCode = "403", description = "Origin is not allowed")
    @ApiResponse(responseCode = "404", description = "Customer cookie does not authorize this active session")
    public ResponseEntity<CustomerBillStatusResponse> request(@PathVariable Long sessionId,
            @CookieValue(name = CustomerSessionAccessService.COOKIE_NAME, required = false) String credential,
            @RequestHeader(name = "Origin", required = false) String origin) {
        origins.requireAllowed(origin);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.request(sessionId, credential));
    }
}
